package som.odata.profile.utils.popup.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;

import som.odata.profile.utils.ODataDefaultProfileUtils;

public class AutoApplyProfileAction implements IObjectActionDelegate {

	private Shell shell;

	private IFile file;

	/**
	 * Constructor for Action1.
	 */
	public AutoApplyProfileAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.getResource(URI.createPlatformResourceURI(file.getFullPath().toString(), true), true);
		List<Package> packages = new ArrayList<Package>();
		for (EObject eObject : resource.getContents()) {
			if (eObject instanceof Package) {
				packages.add((Package) eObject);
			}
		}
		for (Package pkg : packages) {
			// pathmap://ODA_PROFILES/odata.profile.uml#_p6kjUO-pEeaLcvwqpORGRg
			Resource profileResource = resourceSet.getResource(URI.createURI("pathmap://ODA_PROFILES/odata.profile.uml"), true);
			Profile profile = (Profile) profileResource.getEObject("_pWtvsO-mEeaLcvwqpORGRg");
			
			pkg.applyProfile(profile);
			ODataDefaultProfileUtils.applyODServiceStereotype(pkg);
			for (Iterator<EObject> it = pkg.eAllContents(); it.hasNext();) {
				EObject child = it.next();
				if (child instanceof Class) {
					Class clazz = (Class) child;
					ODataDefaultProfileUtils.applyODEntityType(clazz);
					ODataDefaultProfileUtils.applyODEntitySet(clazz);
					
				}
				if (child instanceof Property) {
					Property property = (Property) child;
					ODataDefaultProfileUtils.applyODProperty(property);
					ODataDefaultProfileUtils.applyODataNavigationProperty(property);
					ODataDefaultProfileUtils.applyODataNavigationPropertyBinding(property);
					
				}
				if(child instanceof DataType){
					DataType dataType = (DataType) child;
					if(child instanceof PrimitiveType)
						ODataDefaultProfileUtils.applyODPrimitiveType(dataType);
					else 
						if (child instanceof Enumeration)
							ODataDefaultProfileUtils.applyODEnumType(dataType);
						else
							ODataDefaultProfileUtils.applyODComplexType(dataType);
				}
				
			}
		}
		//resolve basetype
		for (Package pkg : packages) {
			for (Iterator<EObject> it = pkg.eAllContents(); it.hasNext();) {
				EObject child = it.next();
				if (child instanceof Class ) {
					Class clazz = (Class) child;
					ODataDefaultProfileUtils.resolveBaseType(clazz);
					
				}
			}
		}
		try {
			resource.save(Collections.emptyMap());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (structuredSelection.getFirstElement() instanceof IFile) {
				file = (IFile) structuredSelection.getFirstElement();
			}
		}
	}

}
