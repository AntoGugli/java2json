/*
 * Created by Antonino Enrico Guglielmino on 2026.
 * Copyright (c) 2026. Licensed under the Apache License, Version 2.0.
 */
package gugli.anto.serializer.actions

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import gugli.anto.serializer.toolWindow.MyToolWindowFactory

class SerializeClassAction : AnAction() {

    private val objectMapper = ObjectMapper().apply {
        enable(SerializationFeature.INDENT_OUTPUT)

        val printer = DefaultPrettyPrinter()
        val indenter = DefaultIndenter("    ", DefaultIndenter.SYS_LF)

        printer.indentObjectsWith(indenter)
        printer.indentArraysWith(indenter)

        setDefaultPrettyPrinter(printer)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val panel = MyToolWindowFactory.getOrCreatePanel(project)

        ApplicationManager.getApplication().executeOnPooledThread {
            com.intellij.openapi.application.runReadAction {
                val json = generateJson(project)
                ApplicationManager.getApplication().invokeLater {
                    panel.updateContent(json)
                }
            }
        }
    }

    /**
     * Main logic for generating JSON
     */
    fun generateJson(project: Project): String {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return "No active editor"
        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return "Unrecognized file"

        val psiClass = PsiTreeUtil.findChildOfType(psiFile, PsiClass::class.java)
            ?: return "No Java classes found in the current file"

        return try {
            val classMap = classToMap(psiClass, mutableSetOf())
            objectMapper.writer(objectMapper.serializationConfig.defaultPrettyPrinter)
                .writeValueAsString(classMap)
        } catch (ex: Exception) {
            "Error during serialization: ${ex.message}"
        }
    }

    /**
     * Iterate through the fields of PsiClass recursively
     */
    private fun classToMap(psiClass: PsiClass, visited: MutableSet<String>): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val className = psiClass.qualifiedName ?: "Unknown"

        // Protezione da riferimenti circolari
        if (visited.contains(className)) {
            return mapOf("error" to "Circular reference to $className")
        }
        visited.add(className)

        for (field in psiClass.allFields) {
            if (field.name == "serialVersionUID" || field.hasModifierProperty(PsiModifier.STATIC)) {
                continue
            }
            map[field.name] = getFieldValue(field.type, visited)
        }
        return map
    }

    /**
     * Resolves the placeholder value based on the field type
     */
    private fun getFieldValue(type: PsiType, visited: MutableSet<String>): Any? {
        val canonicalText = type.canonicalText

        when {
            type == PsiTypes.intType() || canonicalText == "java.lang.Integer" -> return 0
            type == PsiTypes.booleanType() || canonicalText == "java.lang.Boolean" -> return false
            type == PsiTypes.doubleType() || canonicalText == "java.lang.Double" -> return 0.0
            type == PsiTypes.longType() || canonicalText == "java.lang.Long" -> return 0L
            canonicalText == "java.lang.String" -> return "string"
        }

        if (type is PsiClassType) {
            val resolvedClass = type.resolve() ?: return null

            //ENUM: returns the name of the first constant
            if (resolvedClass.isEnum) {
                return resolvedClass.fields.filterIsInstance<PsiEnumConstant>()
                    .firstOrNull()?.name ?: "ENUM_VALUE"
            }

            //Collections (List, Set, etc.)
            if (isInheritor(resolvedClass, "java.util.Collection")) {
                val genericType = type.parameters.firstOrNull()
                return if (genericType != null) listOf(getFieldValue(genericType, visited)) else emptyList<Any>()
            }

            //Maps
            if (isInheritor(resolvedClass, "java.util.Map")) {
                val valueType = type.parameters.getOrNull(1)
                return mapOf("key" to (valueType?.let { getFieldValue(it, visited) } ?: "value"))
            }

            //Custom Class Case (Recursion) - Excluding java.* system classes
            val qName = resolvedClass.qualifiedName
            if (!qName.isNullOrEmpty() && !qName.startsWith("java.")) {
                return classToMap(resolvedClass, HashSet(visited))
            }

            return "Object<${resolvedClass.name}>"
        }

        //Array []
        if (type is PsiArrayType) {
            return listOf(getFieldValue(type.componentType, visited))
        }

        return null
    }

    /**
     * Check whether a class inherits from another (e.g., java.util.List)
     */
    private fun isInheritor(psiClass: PsiClass, baseClassName: String): Boolean {
        if (psiClass.qualifiedName == baseClassName) return true
        return psiClass.supers.any { isInheritor(it, baseClassName) }
    }
}
