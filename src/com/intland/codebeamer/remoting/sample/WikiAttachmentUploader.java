package com.intland.codebeamer.remoting.sample;

import java.io.*;
import java.net.MalformedURLException;

import com.intland.codebeamer.persistence.dto.*;
import com.intland.codebeamer.persistence.util.BinaryStreamDtoHelper;
import com.intland.codebeamer.remoting.*;
import com.intland.codebeamer.remoting.bean.*;


/**
 * This program is part of the CodeBeamer SDK.
 * <p>
 * This console application illustrates:
 * <ul>
 *   <li>connecting to CodeBeamer</li>
 *   <li>signing in as a CodeBeamer user</li>
 *   <li>uploading image attachments to an existing Wiki page</li>
 * </ul>
 *
 * @author <a href="mailto:robert.enyedi@intland.com">Robert Enyedi</a>
 * @version $Id: zsolt 2009-11-27 19:54 +0100 23955:cdecf078ce1f  $
 */
public class WikiAttachmentUploader {
	/** Main entry point. */
	public static void main(String[] args) throws Exception {
		if(args.length != 5) {
			System.err.println("Usage: <program> service-URL login password page-id image-dir");
			System.err.println("From the image directory all the GIF, JPG and PNG images " +
								"will be uploaded and attached to the specified page.");
			System.exit(-1);
		}
		String serviceUrl = args[0];
		String login = args[1];
		String password = args[2];
		String pageId = args[3];
		String imageDir = args[4];

		upload(serviceUrl, login, password, pageId, imageDir);
	}

	private static void upload(String serviceUrl, String login, String password, String pageId, String imageDir) throws MalformedURLException {
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
		UserDto user = api.getSessionUser(token);

		WikiPageDto wikiPage = api.findWikiPageById(token, new Integer(Integer.parseInt(pageId)));

		if(wikiPage == null) {
			System.out.println("Error: Could not find wiki page with name " + pageId);
		} else {
			System.out.println("Wiki page found. Creating attachments.");

			File file = new File(imageDir);

			if(file.exists() && file.isDirectory()) {
				File[] imageFiles = file.listFiles(new FilenameFilter() {

					public boolean accept(File dir, String name) {
						String lowerCaseName = name.toLowerCase();

						return	lowerCaseName.endsWith(".jpg")
								|| lowerCaseName.endsWith(".gif")
								|| lowerCaseName.endsWith(".png");
					}
				});

				System.out.println("Starting upload for " + imageFiles.length + " image files.");

				for(int i=0;i<imageFiles.length;i++) {
					File file2 = imageFiles[i];

					System.out.println("Uploading file " + (i+1) + "/"
										+ imageFiles.length + ": "
										+ imageFiles[i].getName()
										+ " (" + (imageFiles[i].length()/1024) + " KB)");

					try {
						BinaryStreamDto blob = BinaryStreamDtoHelper.loadFromFile(file2);

						ArtifactDto attachment = new ArtifactDto();
						attachment.setTypeId(Integer.valueOf(ArtifactType.ATTACHMENT));
						attachment.setParent(wikiPage);
						attachment.setDescription("Image attachment uploaded using the RemoteAPI.");
						attachment.setName(file2.getName());
						attachment.setMimeType(getMimeType(file2.getName()));
						attachment.setFileSize(new Long(file2.length()));
						attachment.setOwner(user);

						attachment = api.createArtifactWithBody(token, attachment, blob);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			} else {
				System.out.println("Error: Could not find image directory.");
			}
		}

		System.out.println("Signing out...");
		api.logout(token);

		System.out.println("Done");
	}

	private static String getMimeType(String name) {
		String lowercaseName = name.toLowerCase();

		if(lowercaseName.endsWith(".jpg")) {
			return "image/jpeg";
		}

		return "image/" + lowercaseName.substring(lowercaseName.lastIndexOf('.') + 1);
	}
}
