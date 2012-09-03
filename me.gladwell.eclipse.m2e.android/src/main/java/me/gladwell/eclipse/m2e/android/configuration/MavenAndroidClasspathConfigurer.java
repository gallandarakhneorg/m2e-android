/*******************************************************************************
 * Copyright (c) 2012 Ricardo Gladwell
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package me.gladwell.eclipse.m2e.android.configuration;

import java.io.File;
import java.util.List;

import me.gladwell.eclipse.m2e.android.project.AndroidProject;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IClasspathDescriptor.EntryFilter;
import org.eclipse.m2e.jdt.IClasspathEntryDescriptor;
import org.eclipse.m2e.jdt.IClasspathManager;

import com.android.sdklib.SdkConstants;

public class MavenAndroidClasspathConfigurer implements AndroidClasspathConfigurer {

	public void addGenFolder(IJavaProject javaProject, AndroidProject project, IClasspathDescriptor classpath) {
        IFolder gen = javaProject.getProject().getFolder(SdkConstants.FD_GEN_SOURCES + File.separator);
        if (!gen.exists()) {
            try {
                gen.create(true, true, new NullProgressMonitor());
            } catch (CoreException e) {
                throw new ProjectConfigurationException(e);
            }
        }

        if (!classpath.containsPath(new Path(SdkConstants.FD_GEN_SOURCES))) {
			classpath.addSourceEntry(gen.getFullPath(), null, false);
		}
	}

	public void removeNonRuntimeDependencies(AndroidProject project, IClasspathDescriptor classpath) {
		final List<String> providedDependencies = project.getProvidedDependencies();

		classpath.removeEntry(new EntryFilter() {
			public boolean accept(IClasspathEntryDescriptor descriptor) {
				return providedDependencies.contains(descriptor.getPath().toOSString());
			}
		});
	}

	public void removeJreClasspathContainer(IClasspathDescriptor classpath) {
		for(IClasspathEntry entry : classpath.getEntries()) {
			if(entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
            	if(entry.getPath().toOSString().contains(JavaRuntime.JRE_CONTAINER)) {
            		classpath.removeEntry(entry.getPath());
    			}
			}
		}
	}

	public void markMavenContainerExported(IClasspathDescriptor classpath) {
		for(IClasspathEntry entry : classpath.getEntries()) {
			if(entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
            	if(entry.getPath().toOSString().equals(IClasspathManager.CONTAINER_ID)) {
            		IClasspathEntry newEntry = JavaCore.newContainerEntry(entry.getPath(), true);
            		classpath.removeEntry(entry.getPath());
            		classpath.addEntry(newEntry);
    			}
			}
		}
	}

}
