/*
 * Created by Antonino Enrico Guglielmino on 2026.
 * Copyright (c) 2026. Licensed under the Apache License, Version 2.0.
 */
package gugli.anto.serializer.ui

import com.intellij.find.EditorSearchSession
import com.intellij.find.FindManager
import com.intellij.find.FindModel
import com.intellij.lang.Language
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.LanguageTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JPanel

class SerializedJsonPanel(val project: Project) : JPanel(BorderLayout()) {

    private val jsonLanguage = Language.findLanguageByID("JSON") ?: Language.ANY

    val editorField = object : LanguageTextField(jsonLanguage, project, "", false) {
        override fun createEditor(): EditorEx {
            val editor = super.createEditor()
            editor.setVerticalScrollbarVisible(true)
            editor.setHorizontalScrollbarVisible(true)

            //Managing keyboard shortcuts (Ctrl+F o Cmd+F)
            editor.contentComponent.addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    val isShortcutModifier = if (SystemInfo.isMac) e.isMetaDown else e.isControlDown

                    if (isShortcutModifier && e.keyCode == KeyEvent.VK_F) {
                        e.consume()
                        showSearchPanel(editor)
                    }
                }
            })
            return editor
        }
    }

    init {
        layout = BorderLayout()
        border = JBUI.Borders.empty()
        add(editorField, BorderLayout.CENTER)
    }

    private fun showSearchPanel(editor: EditorEx) {
        val findManager = FindManager.getInstance(project)
        val findModel = FindModel().apply {
            copyFrom(findManager.findInFileModel)
            isReplaceState = false
            isWholeWordsOnly = false
        }

        //Launch the native search session (supports arrow keys, Enter, and Esc)
        EditorSearchSession.start(editor, findModel, project)
    }

    fun updateContent(json: String) {
        editorField.text = json
    }
}
