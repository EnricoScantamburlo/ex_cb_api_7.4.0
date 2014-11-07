/*
 * Copyright by Intland Software
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Intland Software. ("Confidential Information"). You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Intland.
 */
package com.intland.codebeamer.remoting.sample;

import java.io.File;
import java.io.IOException;

import com.intland.codebeamer.persistence.dto.ArtifactDto;
import com.intland.codebeamer.persistence.dto.BinaryStreamDto;
import com.intland.codebeamer.persistence.dto.ProjectDto;
import com.intland.codebeamer.persistence.util.BinaryStreamDtoHelper;
import com.intland.codebeamer.remoting.RemoteApi;
import com.intland.codebeamer.remoting.RemoteApiFactory;
import com.intland.codebeamer.remoting.bean.ServerInfo;


/**
 * This program is part of the CodeBeamer SDK.
 * <p>
 * This console application illustrates:
 * <ul>
 *   <li>connecting to CodeBeamer</li>
 *   <li>signing in as a CodeBeamer user</li>
 *   <li>searching for a document in a project</li>
 *   <li>downloading the document and saving its content to a local file</li>
 * </ul>
 *
 * @author <a href="mailto:aron.gombas@intland.com">Aron Gombas</a>
 * @version $Id: KlausMehling 2007-09-04 10:52 +0000 14350:8928a2398cc1  $
 */
public class ArtifactDownloader {
	/** Main entry point. */
	public static void main(String[] args) throws Exception {
		if(args.length != 5) {
			System.err.println("Usage: <program> service-URL login password project-name artifact-name");
			System.exit(-1);
		}
		String serviceUrl = args[0];
		String login = args[1];
		String password = args[2];
		String projectName = args[3];
		String artifactName = args[4];

		System.out.println("Connecting to CodeBeamer web service at " + serviceUrl + "...");
		RemoteApi api = RemoteApiFactory.getInstance().connect(serviceUrl);
		if(api == null) {
			System.err.println("Couldn't connect, is the service URL correct?");
			System.exit(-1);
		}

		System.out.println("Signing in...");
		String token = api.login(login, password);
		ServerInfo serverInfo = api.getServerInfo();
		System.out.println("Signed in to CodeBeamer " + serverInfo.getMajorVersion() + serverInfo.getMinorVersion() + " (" + serverInfo.getBuildDate() + ") running on " + serverInfo.getOs() + "/Java " + serverInfo.getJavaVersion());

		System.out.println("Searching for project \"" + projectName + "\"...");
		ProjectDto project = findProjectByName(api.findAllProjects(token), projectName);

		if(project != null) {
			System.out.println("Searching for artifact \"" + artifactName + "\"...");
			ArtifactDto artifact = findArtifactByName(api, token, api.findTopArtifactsByProject(token, project.getId()), artifactName);

			if(artifact != null) {
				System.out.print("Downloading...");
				BinaryStreamDto data = api.getArtifactBody(token, artifact.getId());

				if(data != null) {
					try {
						BinaryStreamDtoHelper.saveToFile(data, new File(data.getFileName()));
					} catch(IOException ex) {
						System.err.println(ex);
					}
					System.out.println(" " + data.getLength() + " bytes");
				} else {
					System.err.println(" Empty content, are the artifact name and access rights correct?");
				}
			} else {
				System.err.println("Couldn't find artifact \"" + artifactName + "\" in project \"" + projectName + "\"");
			}
		} else {
			System.err.println("Couldn't find project \"" + projectName + "\"");
		}

		System.out.println("Signing out...");
		api.logout(token);

		System.out.println("Done");
	}

	/** Returns the project with the given name. */
	protected static ProjectDto findProjectByName(ProjectDto projects[], String projectName) {
		for(int i = 0; i < projects.length; i++)
			if(projects[i].getName().equals(projectName))
				return projects[i];

		return null;
	}

	/**
	 * Recursively search for the artifact with the given name.
	 * @return the artifact with the given name or <code>null</code> if not found.
	 */
	protected static ArtifactDto findArtifactByName(RemoteApi api, String token, ArtifactDto artifacts[], String artifactName) {
		// search in these
		for(int i = 0; i < artifacts.length; i++) {
			if(artifacts[i].getName().equals(artifactName))
				return artifacts[i];

			// search in the children
			if(artifacts[i].isDirectory()) {
				ArtifactDto foundInChildren = findArtifactByName(api, token, api.findArtifactsByParentArtifact(token, artifacts[i].getId()), artifactName);
				if(foundInChildren != null)
					return foundInChildren;
			}
		}

		return null;
	}
}
