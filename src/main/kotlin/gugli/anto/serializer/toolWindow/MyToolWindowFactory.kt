/*
 * Created by Antonino Enrico Guglielmino on 2026.
 * Copyright (c) 2026. Licensed under the Apache License, Version 2.0.
 */
package gugli.anto.serializer.toolWindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.ui.content.ContentFactory
import gugli.anto.serializer.actions.SerializeClassAction
import gugli.anto.serializer.ui.SerializedJsonPanel

class MyToolWindowFactory : ToolWindowFactory {

    companion object {
        private val panels = mutableMapOf<Project, SerializedJsonPanel>()
        fun getOrCreatePanel(project: Project): SerializedJsonPanel {
            return panels.getOrPut(project) { SerializedJsonPanel(project) }
        }
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = getOrCreatePanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)

        // Listener for automatic updates on startup
        project.messageBus.connect().subscribe(ToolWindowManagerListener.TOPIC, object : ToolWindowManagerListener {
            override fun stateChanged(toolWindowManager: com.intellij.openapi.wm.ToolWindowManager) {
                if (toolWindow.isVisible) {
                    triggerSerialization(project, panel)
                }
            }
        })
    }

    private fun triggerSerialization(project: Project, panel: SerializedJsonPanel) {
        ApplicationManager.getApplication().executeOnPooledThread {
            runReadAction {
                val action = SerializeClassAction()
                val json = action.generateJson(project)
                ApplicationManager.getApplication().invokeLater {
                    panel.updateContent(json)
                }
            }
        }
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}