/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui;

import org.eclipse.cdt.make.ui.actions.UpdateMakeProjectAction;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class MakeStartup extends Job {

	public MakeStartup() {
		super(MakeUIPlugin.getResourceString("MakeUIPlugin.update_project")); //$NON-NLS-1$
		setSystem(true);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
        final IProject[] oldProject = UpdateMakeProjectAction.getOldProjects();
        if (oldProject.length > 0) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    Shell shell = MakeUIPlugin.getDefault().getShell();
                    boolean shouldUpdate = MessageDialog.openQuestion(shell,
                        MakeUIPlugin.getResourceString("MakeUIPlugin.update_project"), //$NON-NLS-1$
                        MakeUIPlugin.getResourceString("MakeUIPlugin.update_project_message")); //$NON-NLS-1$

                    if (shouldUpdate) {
                        ProgressMonitorDialog pd = new ProgressMonitorDialog(shell);
                        UpdateMakeProjectAction.run(false, pd, oldProject);
                    }
                }
            });
        }
        return Status.OK_STATUS;
    }
}
