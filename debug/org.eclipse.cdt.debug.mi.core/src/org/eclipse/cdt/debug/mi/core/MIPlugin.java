/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core;

import java.io.IOException;

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.mi.core.cdi.CSession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MITargetAttach;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;

/**
 * GDB/MI Plugin.
 */
public class MIPlugin extends Plugin {

	//The shared instance.
	private static MIPlugin plugin;

	/**
	 * The constructor
	 * @see org.eclipse.core.runtime.Plugin#Plugin(IPluginDescriptor)
	 */
	public MIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}

	/**
	 * Returns the singleton.
	 */
	public static MIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Method createMISession.
	 * @param Process
	 * @return MISession
	 */
	public MISession createMISession(Process process) throws MIException {
		return new MISession(process);
	}

	/**
	 * Method createMISession.
	 * @param Process
	 * @param PTY
	 * @return MISession
	 */
	public MISession createMISession(Process process, PTY pty) throws MIException {
		return new MISession(process, pty);
	}

	/**
	 * Method createCSession.
	 * @param program
	 * @return ICDISession
	 * @throws IOException
	 */
	public ICDISession createCSession(String program) throws IOException, MIException {
 		String[] args;
		PTY pty = null;
		try {
			pty = new PTY();
			String ttyName = pty.getSlaveName();
			args = new String[]{"gdb", "-q", "-nw", "-tty", ttyName, "-i", "mi", program};
		} catch (IOException e) {
			//e.printStackTrace();
			pty = null;
			args = new String[]{"gdb", "-q", "-nw", "-i", "mi", program};
		}

		Process gdb = ProcessFactory.getFactory().exec(args);
		MISession session = createMISession(gdb, pty);
		/*
		try {
			CommandFactory factory = session.getCommandFactory();
			MIBreakInsert bkpt= factory.createMIBreakInsert(true, false, null, 0, "main");
			session.postCommand(bkpt);
			MIInfo info = bkpt.getMIInfo();
			if (info == null) {
				throw new IOException("No answer");
			}
		} catch (MIException e) {
			throw new IOException("Failed to attach");
		}
		*/
		return new CSession(session, false);
	}

	/**
	 * Method createCSession.
	 * @param program
	 * @param core
	 * @return ICDISession
	 * @throws IOException
	 */
	public ICDISession createCSession(String program, String core) throws IOException, MIException {
		String[]args = new String[]{"gdb", "--quiet", "-nw", "-i", "mi", program, core};
		Process gdb = ProcessFactory.getFactory().exec(args);
		MISession session = createMISession(gdb);
		return new CSession(session);
	}

	/**
	 * Method createCSession.
	 * @param program
	 * @param pid
	 * @return ICDISession
	 * @throws IOException
	 */
	public ICDISession createCSession(String program, int pid) throws IOException, MIException {
		String[]args = new String[]{"gdb", "--quiet", "-nw", "-i", "mi", program};
		Process gdb = ProcessFactory.getFactory().exec(args);
		MISession session = createMISession(gdb);
		try {
			CommandFactory factory = session.getCommandFactory();
			MITargetAttach attach = factory.createMITargetAttach(pid);
			session.postCommand(attach);
			MIInfo info = attach.getMIInfo();
			if (info == null) {
				throw new IOException("No answer");
			}
			//@@@ We have to manually set the suspended state when we attach
			//(assuming the attach works) as a safeguard since GDB man not
			//notice the attach (the behaviour in gdb 5.0).
			session.getMIInferior().setSuspended();
		} catch (MIException e) {
			throw new IOException("Failed to attach");
		}
		return new CSession(session, true);
	}
	
	
	public static void debugLog(String message) {
	//	if ( getDefault().isDebugging() ) {
	//		getDefault().getLog().log(StatusUtil.newStatus(Status.ERROR, message, null));
			if (message.endsWith("\n")) {
				System.err.print(message);
			} else {
				System.err.println(message);
			}
	//	}
	}
}
