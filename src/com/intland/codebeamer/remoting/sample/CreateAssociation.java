package com.intland.codebeamer.remoting.sample;

import com.intland.codebeamer.persistence.dto.AssociationDto;
import com.intland.codebeamer.persistence.dto.ProjectDto;
import com.intland.codebeamer.remoting.RemoteApi;
import com.intland.codebeamer.remoting.RemoteApiFactory;
import com.intland.codebeamer.remoting.bean.ServerInfo;
import com.sun.deploy.association.Association;


/**
 * This program is part of the CodeBeamer SDK.
 * <p>
 * This console application illustrates:
 * <ul>
 *   <li>connecting to CodeBeamer</li>
 *   <li>signing in as a CodeBeamer user</li>
 *   <li>listing the existing projects</li>
 * </ul>
 *
 * @author <a href="mailto:aron.gombas@intland.com">Aron Gombas</a>
 * @version $Id$
 */
public class CreateAssociation {
	/** Main entry point. */
	public static void main(String[] args) throws Exception {
		if(args.length != 3) {
			System.err.println("Usage: <program> service-URL login password");
			System.exit(-1);
		}
		String serviceUrl = args[0];
		String login = args[1];
		String password = args[2];

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

		ProjectDto projects[] = api.findAllProjects(token);
		for(int i = 0; i < projects.length; i++) {
            ProjectDto project = projects[i];
            System.out.println(project.getName());
		}
        api.findT

        AssociationDto associations[] = api.findAllAssociations(token);
        for (int i=0; i<associations.length; i++) {
            AssociationDto association = associations[i];
            System.out.println(association.getId() + ": " + association.getType().getName());
        }


		System.out.println("Signing out...");
		api.logout(token);

		System.out.println("Done");
	}
}
