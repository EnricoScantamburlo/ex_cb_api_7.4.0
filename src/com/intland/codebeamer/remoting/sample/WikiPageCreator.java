package com.intland.codebeamer.remoting.sample;


import com.intland.codebeamer.persistence.dto.*;
import com.intland.codebeamer.remoting.*;
import com.intland.codebeamer.remoting.bean.*;


/**
 * This program is part of the CodeBeamer SDK.
 * <p>
 * This console application illustrates:
 * <ul>
 *   <li>connecting to CodeBeamer</li>
 *   <li>signing in as a CodeBeamer user</li>
 *   <li>creating a new wiki page in the project and adding it as a child to the root Wiki page</li>
 * </ul>
 *
 * @author <a href="mailto:robert.enyedi@intland.com">Robert Enyedi</a>
 * @version $Id: zsolt 2009-11-27 19:54 +0100 23955:cdecf078ce1f  $
 */
public class WikiPageCreator {
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
			WikiPageDto wikiPage = new WikiPageDto();
			wikiPage.setProject(project);
			wikiPage.setName("TestPage");
			wikiPage.setDescription("Wiki page created with the Remote API");

			WikiPageDto[] rootPages = api.findTopWikiPagesByProject(token, project.getId());

			if(rootPages != null && rootPages.length > 0) {
				wikiPage.setParent(rootPages[0]);

				wikiPage = api.createWikiPage(token, wikiPage, "This is the content of the page", "Created by WikiPageCreator", DescriptionFormat.PLAIN_TEXT);

				if(wikiPage == null) {
					System.out.println("Error: Could not create wiki page!");
				} else {
					System.out.println("Wiki page created successfully: " + wikiPage.getName());
				}
			}
			else {
				System.out.println("Error: Could not find root wiki page!");
			}
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
