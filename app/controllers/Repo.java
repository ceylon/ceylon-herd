package controllers;

import models.Module;
import models.ModuleVersion;
import models.User;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.libs.MimeTypes;
import play.mvc.Before;
import util.JavaExtensions;
import util.Util;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Repo extends MyController {

	// we set it if it's there, otherwise we don't require it
	@Before
    static void setConnectedUser() {
        if(Security.isConnected()) {
            User user = User.find("byUserName", Security.connected()).first();
            renderArgs.put("user", user);
        }
    }

	public static void index(){
		List<models.Module> modules = models.Module.find("ORDER BY name").fetch();
		
		render(modules);
	}

	public static void versions(@Required String moduleName){
		if(validationFailed()){
			index();
		}
		models.Module module = models.Module.findByName(moduleName);
		if(module == null){
			Validation.addError(null, "Unknown module");
			prepareForErrorRedirect();
			index();
		}
		List<models.ModuleVersion> versions = models.ModuleVersion.findByModule(module);
		
		render(module, versions);
	}

	public static void search(String q){
		if(StringUtils.isEmpty(q))
			index();
		
		List<models.Module> modules = models.Module.searchByName(q);
		
		render(modules, q);
	}

	public static void view(@Required String moduleName, @Required String version){
		models.ModuleVersion moduleVersion = getModuleVersion(moduleName, version);
		models.Module module = moduleVersion.module;
		
		render(module, moduleVersion);
	}

	public static void viewDoc(@Required String moduleName, @Required String version){
	    models.ModuleVersion moduleVersion = getModuleVersion(moduleName, version);
	    models.Module module = moduleVersion.module;

	    render(module, moduleVersion);
	}

	private static ModuleVersion getModuleVersion(String moduleName, String version) {
		if(validationFailed())
			index();
		models.ModuleVersion moduleVersion = models.ModuleVersion.findByVersion(moduleName, version);
		if(moduleVersion == null){
			Validation.addError(null, "Unknown module");
			prepareForErrorRedirect();
			index();
		}
		return moduleVersion;
	}

	public static void viewFile(String path) throws IOException{
		File repoDir = Util.getRepoDir();
		File file = new File(repoDir, path);
		checkPath(file, repoDir);
		
		if(!file.exists())
			notFound(path);
		
		if(file.isDirectory()){
		    // try a module version
		    ModuleVersion moduleVersion = findModuleVersion(file);
		    Module module = null;
		    if(moduleVersion == null){
		        // try a module
		        module= findModule(file);
		    }
			render("Repo/viewFile.html", file, moduleVersion, module);
		}else{
		    response.contentType = MimeTypes.getContentType(file.getName());
		    increaseStats(file);
			renderBinary(file);
		}
	}

	private static Module findModule(File file) {
	    for(File f : file.listFiles()){
	        if(!f.isDirectory()){
	            // fail fast: if we have a file we're not in a module dir
	            return null;
	        }
	        // look for a ModuleVersion in the first folder we find
	        ModuleVersion v = findModuleVersion(f);
	        // fail fast if there's no ModuleVersion there, it means we're not in a module dir
	        return v != null ? v.module : null;
	    }
	    return null;
	}

	private static ModuleVersion findModuleVersion(File file) {
	    for(File f : file.listFiles()){
	        if(f.isDirectory()){
	            // fail fast: if we have a directory and it's not "module-doc" we're not in a module version dir
	            if(!f.getName().equals("module-doc"))
	                return null;
	            continue;
	        }
	        ModuleVersion v = findModuleVersion(f.getName(), f, ".car");
	        if(v != null)
	            return v;
	    }
	    return null;
    }

    private static void increaseStats(File file) {
	    String name = file.getName();
	    ModuleVersion mv = findModuleVersion(name, file, ".car");
	    if(mv != null){
	        ModuleVersion.incrementDownloads(mv);
	        return;
	    }
	    mv = findModuleVersion(name, file, ".src");
        if(mv != null){
            ModuleVersion.incrementSourceDownloads(mv);
            return;
        }
	}
	private static ModuleVersion findModuleVersion(String name, File file, String extension){
	    if(!name.endsWith(extension)){
	        return null;
	    }
	    String path = JavaExtensions.relative(file);
	    Logger.debug("Path: %s", path);
	    int lastFileSep = path.lastIndexOf(File.separatorChar);
	    if(lastFileSep == -1){
	        Logger.info("Got a %s without a module? %s", extension, path);
	        return null;
	    }
	    String moduleAndVersion = path.substring(0, lastFileSep);
	    int versionSep = moduleAndVersion.lastIndexOf(File.separatorChar);
	    if(versionSep == -1){
	        Logger.info("Got a %s without a version? %s", extension, path);
	        return null;
	    }
	    String moduleName = moduleAndVersion.substring(0, versionSep).replace(File.separatorChar, '.');
	    String version = moduleAndVersion.substring(versionSep+1);
	    if(moduleName.isEmpty() || version.isEmpty()){
	        Logger.info("Got a %s with empty name or version? %s", extension, path);
	        return null;
	    }
	    String expectedName = moduleName+"-"+version+extension;
	    if(!name.equals(expectedName)){
	        Logger.info("%s name %s doesn't match expected name %s", extension, name, expectedName);
	        return null;
	    }
	    Logger.debug("We got a %s", extension);
	    ModuleVersion moduleVersion = ModuleVersion.findByVersion(moduleName, version);
	    if(moduleVersion == null){
	        Logger.info("Failed to find a ModuleVersion for %s/%s", moduleName, version);
	        return null;
	    }
	    return moduleVersion;
    }

    public static void noFile() throws IOException{
	    render();
	}

	private static void checkPath(File file, File repoDir) throws IOException{
		String repoPath = repoDir.getCanonicalPath();
		String filePath = file.getCanonicalPath();
		if(!filePath.startsWith(repoPath))
			forbidden("Path is not valid");
	}

	public static void myModules(){
		User user = User.find("byUserName", Security.connected()).first();
		List<models.Module> modules = models.Module.findByOwner(user);
		boolean myModule =true;
		flash.put("myModule", true);
		index();
	}
}