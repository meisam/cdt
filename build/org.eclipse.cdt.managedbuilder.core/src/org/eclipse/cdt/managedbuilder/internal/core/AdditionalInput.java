/*******************************************************************************
 * Copyright (c) 2005, 2011 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.core.SafeStringInterner;
import org.eclipse.cdt.managedbuilder.core.IAdditionalInput;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;

public class AdditionalInput implements IAdditionalInput {

	private static final String EMPTY_STRING = new String();

	//  Superclass
	//  Parent and children
	private IInputType fParent;
	//  Managed Build model attributes
	private String fPaths;
	private Integer fKind;
	//  Miscellaneous
	private boolean fIsExtensionAdditionalInput = false;
	private boolean fIsDirty = false;
	private boolean fResolved = true;
	private boolean fRebuildState;

	/*
	 *  C O N S T R U C T O R S
	 */
	
	/**
	 * This constructor is called to create an AdditionalInput defined by an extension point in 
	 * a plugin manifest file, or returned by a dynamic element provider
	 * 
	 * @param parent  The IInputType parent of this AdditionalInput
	 * @param element The AdditionalInput definition from the manifest file or a dynamic element
	 *                provider
	 */
	public AdditionalInput(IInputType parent, IManagedConfigElement element) {
		this.fParent = parent;
		fIsExtensionAdditionalInput = true;
		
		// setup for resolving
		fResolved = false;

		loadFromManifest(element);
	}

	/**
	 * This constructor is called to create an AdditionalInput whose attributes and children will be 
	 * added by separate calls.
	 * 
	 * @param parent The parent of the an AdditionalInput
	 * @param isExtensionElement Indicates whether this is an extension element or a managed project element
	 */
	public AdditionalInput(InputType parent, boolean isExtensionElement) {
		this.fParent = parent;
		fIsExtensionAdditionalInput = isExtensionElement;
		if (!isExtensionElement) {
			setDirty(true);
			setRebuildState(true);
		}
	}

	/**
	 * Create an <code>AdditionalInput</code> based on the specification stored in the 
	 * project file (.cdtbuild).
	 * 
	 * @param parent The <code>ITool</code> the AdditionalInput will be added to. 
	 * @param element The XML element that contains the AdditionalInput settings.
	 */
	public AdditionalInput(IInputType parent, ICStorageElement element) {
		this.fParent = parent;
		fIsExtensionAdditionalInput = false;
		
		// Initialize from the XML attributes
		loadFromProject(element);
	}

	/**
	 * Create an <code>AdditionalInput</code> based upon an existing AdditionalInput.
	 * 
	 * @param parent The <code>IInputType</code> the AdditionalInput will be added to.
	 * @param additionalInput The existing AdditionalInput to clone.
	 */
	public AdditionalInput(IInputType parent, AdditionalInput additionalInput) {
		this.fParent = parent;
		fIsExtensionAdditionalInput = false;
		
		//  Copy the remaining attributes
		if (additionalInput.fPaths != null) {
			fPaths = new String(additionalInput.fPaths);
		}

		if (additionalInput.fKind != null) {
			fKind = new Integer(additionalInput.fKind.intValue());
		}
		
		setDirty(true);
		setRebuildState(true);
	}

	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */
	
	/* (non-Javadoc)
	 * Loads the AdditionalInput information from the ManagedConfigElement specified in the 
	 * argument.
	 * 
	 * @param element Contains the AdditionalInput information 
	 */
	protected void loadFromManifest(IManagedConfigElement element) {

		// path
		fPaths = SafeStringInterner.safeIntern(element.getAttribute(IAdditionalInput.PATHS));

		// kind
		String kindStr = element.getAttribute(IAdditionalInput.KIND);
		if (kindStr == null || kindStr.equals(ADDITIONAL_INPUT_DEPENDENCY)) {
			fKind = new Integer(KIND_ADDITIONAL_INPUT_DEPENDENCY);
		} else if (kindStr.equals(ADDITIONAL_INPUT)) {
			fKind = new Integer(KIND_ADDITIONAL_INPUT);
		} else if (kindStr.equals(ADDITIONAL_DEPENDENCY)) {
			fKind = new Integer(KIND_ADDITIONAL_DEPENDENCY);
		}
	}
	
	/* (non-Javadoc)
	 * Initialize the AdditionalInput information from the XML element 
	 * specified in the argument
	 * 
	 * @param element An XML element containing the AdditionalInput information 
	 */
	protected void loadFromProject(ICStorageElement element) {
		
		// path
		if (element.getAttribute(IAdditionalInput.PATHS) != null) {
			fPaths = SafeStringInterner.safeIntern(element.getAttribute(IAdditionalInput.PATHS));
		}
		
		// kind
		if (element.getAttribute(IAdditionalInput.KIND) != null) {
			String kindStr = element.getAttribute(IAdditionalInput.KIND);
			if (kindStr == null || kindStr.equals(ADDITIONAL_INPUT_DEPENDENCY)) {
				fKind = new Integer(KIND_ADDITIONAL_INPUT_DEPENDENCY);
			} else if (kindStr.equals(ADDITIONAL_INPUT)) {
				fKind = new Integer(KIND_ADDITIONAL_INPUT);
			} else if (kindStr.equals(ADDITIONAL_DEPENDENCY)) {
				fKind = new Integer(KIND_ADDITIONAL_DEPENDENCY);
			}
		}
	}

	/**
	 * Persist the AdditionalInput to the project file.
	 */
	public void serialize(ICStorageElement element) {

		if (fPaths != null) {
			element.setAttribute(IAdditionalInput.PATHS, fPaths);
		}

		if (fKind != null) {
			String str;
			switch (getKind()) {
				case KIND_ADDITIONAL_INPUT:
					str = ADDITIONAL_INPUT;
					break;
				case KIND_ADDITIONAL_DEPENDENCY:
					str = ADDITIONAL_DEPENDENCY;
					break;
				case KIND_ADDITIONAL_INPUT_DEPENDENCY:
					str = ADDITIONAL_INPUT_DEPENDENCY;
					break;
				default:
					str = EMPTY_STRING; 
					break;
			}
			element.setAttribute(IAdditionalInput.KIND, str);
		}
		
		// I am clean now
		fIsDirty = false;
	}

	/*
	 *  P A R E N T   A N D   C H I L D   H A N D L I N G
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IAdditionalInput#getParent()
	 */
	public IInputType getParent() {
		return fParent;
	}

	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IAdditionalInput#getPaths()
	 */
	public String[] getPaths() {
		if (fPaths == null) {
			return null;
		}
		String[] nameTokens = CDataUtil.stringToArray(fPaths, ";"); //$NON-NLS-1$
		return nameTokens;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IAdditionalInput#setPaths()
	 */
	public void setPaths(String newPaths) {
		if (fPaths == null && newPaths == null) return;
		if (fPaths == null || newPaths == null || !(fPaths.equals(newPaths))) {
			fPaths = newPaths;
			fIsDirty = true;
			setRebuildState(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IAdditionalInput#getKind()
	 */
	public int getKind() {
		if (fKind == null) {
			return KIND_ADDITIONAL_INPUT_DEPENDENCY;
		}
		return fKind.intValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IAdditionalInput#setKind()
	 */
	public void setKind(int newKind) {
		if (fKind == null || !(fKind.intValue() == newKind)) {
			fKind = new Integer(newKind);
			fIsDirty = true;
			setRebuildState(true);
		}
	}

	/*
	 *  O B J E C T   S T A T E   M A I N T E N A N C E
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IAdditionalInput#isExtensionElement()
	 */
	public boolean isExtensionElement() {
		return fIsExtensionAdditionalInput;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IAdditionalInput#isDirty()
	 */
	public boolean isDirty() {
		// This shouldn't be called for an extension AdditionalInput
 		if (fIsExtensionAdditionalInput) return false;
		return fIsDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IAdditionalInput#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
		this.fIsDirty = isDirty;
	}
	
	/* (non-Javadoc)
	 *  Resolve the element IDs to interface references
	 */
	public void resolveReferences() {
		if (!fResolved) {
			fResolved = true;
		}
	}
	
	public boolean needsRebuild(){
		return fRebuildState;
	}
	
	public void setRebuildState(boolean rebuild){
		if(isExtensionElement() && rebuild)
			return;

		fRebuildState = rebuild;
	}

}
