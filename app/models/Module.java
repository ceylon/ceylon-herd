package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import play.db.jpa.Model;
import util.VersionComparator;
import controllers.RepoAPI;

@Entity
@SuppressWarnings("serial")
public class Module extends Model {

    public enum Type {
        JVM, JS, SRC;
    }

	public static final Pattern githubPattern = Pattern.compile("https?://github.com/([^/]+)/([^/]+)/?");

	@Column(nullable = false, unique = true)
	public String name;
	
	public String friendlyName;
	public String homeURL;
	public String issueTrackerURL;
	public String codeURL;
	
	@JoinColumn(nullable = false)
	@ManyToOne
	public User owner;
	
	@Sort(comparator = VersionComparator.class, type = SortType.COMPARATOR)
	@OneToMany(mappedBy = "module")
	public SortedSet<ModuleVersion> versions = new TreeSet<ModuleVersion>();

	@ManyToMany
    @JoinTable(name = "module_admin_user",
            	joinColumns = { @JoinColumn(name = "module") },
            	inverseJoinColumns = { @JoinColumn(name = "admin") })
	public List<User> admins = new ArrayList<User>();

	@OrderBy("date")
	@OneToMany(mappedBy="module")
	public List<ModuleComment> comments = new ArrayList<ModuleComment>();
	
	@Transient
	public boolean isGithub(){
		if(StringUtils.isEmpty(codeURL))
			return false;
		Matcher matcher = githubPattern.matcher(codeURL);
		return matcher.matches();
	}

	@Transient
	public String getGithubOwner(){
		if(StringUtils.isEmpty(codeURL))
			return null;
		Matcher matcher = githubPattern.matcher(codeURL);
		if(!matcher.matches())
			return null;
		return matcher.group(1);
	}

	@Transient
	public String getGithubProject(){
		if(StringUtils.isEmpty(codeURL))
			return null;
		Matcher matcher = githubPattern.matcher(codeURL);
		if(!matcher.matches())
			return null;
		return matcher.group(2);
	}

	@Transient
	public Date getLastPublished(){
		return find("SELECT MAX(published) FROM ModuleVersion WHERE module = ?", this).first(); 
	}
	
    @Transient
    public long getDownloads(){
        long ret = 0;
        for(ModuleVersion version : versions){
            ret += version.downloads;
        }
        return ret;
    }

    @Transient
    public long getJsDownloads(){
        long ret = 0;
        for(ModuleVersion version : versions){
            ret += version.jsdownloads;
        }
        return ret;
    }

	@Transient
	public long getSourceDownloads(){
	    long ret = 0;
	    for(ModuleVersion version : versions){
	        ret += version.sourceDownloads;
	    }
	    return ret;
	}

    @Transient
    public String getPath(){
        return name.replace('.', '/');
    }
    
    public boolean canEdit(User user){
		return user != null
				&& (user.equals(owner)
						|| user.isAdmin
						|| admins.contains(user));
	}
	
	public boolean isOwnedBy(User user){
		return user != null
				&& (user.equals(owner)
						|| user.isAdmin);
	}

	public List<ModuleVersion> getVersions(Type type){
	    List<ModuleVersion> ret = new LinkedList<ModuleVersion>();
	    for(ModuleVersion version : versions){
	        boolean include = false;
	        switch(type){
            case JS:
                include = version.isJsPresent;
                break;
            case JVM:
                include = version.isCarPresent || version.isJarPresent;
                break;
            case SRC:
                include = version.isSourcePresent;
                break;
	        }
	        if(include)
	            ret.add(version);
	    }
	    return ret;
	}
	
	public ModuleVersion getLastVersion(){
	    return versions.last();
	}
	
	//
	// Static helpers
	
	public static Module findByName(String moduleName) {
		return find("name = ?", moduleName).first();
	}

    public static List<Module> searchByName(String q) {
        return find("LOCATE(?, name) <> 0", q).fetch();
    }

	public static List<Module> findByOwner(User owner) {
		return find("owner = ? ORDER BY name", owner).fetch();
	}

	public static long countForOwner(User owner) {
		return count("owner = ?", owner);
	}

    public static List<Module> completeForBackend(String module, Type t) {
        if(module == null)
            module = "";
        String typeQuery = ModuleVersion.getBackendQuery("v.", t);
        return Module.find("FROM Module m WHERE LOCATE(?, m.name) = 1"
                + " AND EXISTS(FROM ModuleVersion v WHERE v.module = m AND ("+typeQuery+"))"
                + " ORDER BY name", module).fetch(RepoAPI.RESULT_LIMIT);
    }

    public static long completeForBackendCount(String module, Type t) {
        if(module == null)
            module = "";
        String typeQuery = ModuleVersion.getBackendQuery("v.", t);
        return Module.count("FROM Module m WHERE LOCATE(?, m.name) = 1"
                + " AND EXISTS(FROM ModuleVersion v WHERE v.module = m AND ("+typeQuery+"))"
                , module);
    }

    public static List<Module> searchForBackend(String query, Type t, int start, int count) {
        if(count == 0)
            return Collections.<Module>emptyList();
        
        String typeQuery = ModuleVersion.getBackendQuery("v.", t);
        if(query == null || query.isEmpty()){
            // list
            return Module.find("FROM Module m WHERE"
                    + " EXISTS(FROM ModuleVersion v WHERE v.module = m AND ("+typeQuery+"))"
                    + " ORDER BY name").from(start).fetch(count);
        }
        // FIXME: this smells like the most innefficient SQL request ever made
        // FIXME: we're not searching for author here, but should we?
        return Module.find("FROM Module m WHERE"
                + " EXISTS(FROM ModuleVersion v WHERE v.module = m AND ("+typeQuery+")"
                +                                   " AND (LOCATE(?1, m.name) <> 0"
                +                                          " OR LOCATE(?1, v.doc) <> 0"
                +                                          "))"
                + " ORDER BY name", query).from(start).fetch(count);
    }

    public static long searchForBackendCount(String query, Type t) {
        String typeQuery = ModuleVersion.getBackendQuery("v.", t);
        if(query == null || query.isEmpty()){
            // list
            return Module.count("FROM Module m WHERE"
                    + " EXISTS(FROM ModuleVersion v WHERE v.module = m AND ("+typeQuery+"))");
        }
        // FIXME: this smells like the most innefficient SQL request ever made
        // FIXME: we're not searching for author here, but should we?
        return Module.count("FROM Module m WHERE"
                + " EXISTS(FROM ModuleVersion v WHERE v.module = m AND ("+typeQuery+")"
                +                                   " AND (LOCATE(?1, m.name) <> 0"
                +                                          " OR LOCATE(?1, v.doc) <> 0"
                +                                          "))",
                query);
    }
}
