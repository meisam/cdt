/*******************************************************************************
 * Copyright (c) 2004, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.codan.core.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.codan.core.checkers.sample.StatementHasNoEffectCheckerTest;

public class AutomatedIntegrationSuite extends TestSuite {

	public AutomatedIntegrationSuite() {
	}

	public AutomatedIntegrationSuite(Class theClass, String name) {
		super(theClass, name);
	}

	public AutomatedIntegrationSuite(Class theClass) {
		super(theClass);
	}

	public AutomatedIntegrationSuite(String name) {
		super(name);
	}

	public static Test suite() {

		final AutomatedIntegrationSuite suite = new AutomatedIntegrationSuite();
		
		suite.addTestSuite(StatementHasNoEffectCheckerTest.class);

		return suite;
	}




}