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

import java.util.Date;

import com.intland.codebeamer.persistence.dto.ArtifactDto;
import com.intland.codebeamer.persistence.dto.ArtifactStatusDto;
import com.intland.codebeamer.persistence.dto.ProjectDto;
import com.intland.codebeamer.persistence.dto.UserDto;
import com.intland.codebeamer.persistence.util.BinaryStreamDtoHelper;
import com.intland.codebeamer.remoting.ArtifactType;
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
 *   <li>creating a new folder in the root dir of the project</li>
 *   <li>uploading a new textfile as to this folder with the status "New"</li>
 *   <li>adding a second revision of the same textfile</li>
 * </ul>
 *
 * @author <a href="mailto:aron.gombas@intland.com">Aron Gombas</a>
 * @version $Id: KlausMehling 2008-11-04 15:34 +0000 19201:0d8a66113c22  $
 */
public class ArtifactUploader {
	/** Main entry point. */
	public static void main(String[] args) throws Exception {
		if(args.length != 4) {
			System.err.println("Usage: <program> service-URL login password project-name");
			System.exit(-1);
		}
		String serviceUrl = args[0];
		String login = args[1];
		String password = args[2];
		String projectName = args[3];

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
			String dirName = "API Uploads at " + new Date().toString();
			String docName = "My Document.txt";
			UserDto user = api.getSessionUser(token);

			System.out.println("Creating new directory \"" + dirName + "\"...");
			ArtifactDto dir = new ArtifactDto();
			dir.setProject(project);
			dir.setName(dirName);
			dir.setTypeId(Integer.valueOf(ArtifactType.DIR));
			dir.setDescription("This directory was created through the CodeBeamer API.");
			dir.setOwner(user);
			dir = api.createArtifact(token, dir);

			System.out.println("Uploading artifact \"" + docName + "\"...");
			ArtifactStatusDto status = new ArtifactStatusDto();
			status.setId(new Integer(1)); // TODO where to get NEW from?

			ArtifactDto doc = new ArtifactDto();
			doc.setProject(project);
			doc.setParent(dir);
			doc.setName(docName);
			doc.setTypeId(Integer.valueOf(ArtifactType.FILE));
			doc.setDescription("This document was uploaded through the CodeBeamer API.");
			doc.setOwner(user);
			doc.setStatus(status);
			doc.setComment("Initial upload");
			doc = api.createArtifactWithBody(token, doc, BinaryStreamDtoHelper.createFromBytes("This is the binary content of the file.".getBytes()));

			System.out.println("Uploading new revision of \"" + docName + "\"...");
			api.updateArtifactBody(token, doc.getId(), BinaryStreamDtoHelper.createFromBytes("This is the changed binary content of the file (<strong>second</strong> version).".getBytes()), "Second version.", null);
		} else {
			System.out.println("Couldn't find project \"" + projectName + "\"");
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
}
