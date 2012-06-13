package util;

import models.Module;
import models.Project;
import models.Upload;
import models.User;
import play.cache.Cache;
import play.mvc.Scope.Session;

public class MyCache {

    private static String UserPropertiesDuration = "5mn";
    
    private static String getUserKey(String category){
        return Session.current().getId()+"-"+category;
    }
    
    public static long getProjectsForOwner(User owner){
        String key = getUserKey("projects");
        Long n = Cache.get(key, Long.class);
        if(n == null){
            n = Project.countForOwner(owner);
            Cache.add(key, n, UserPropertiesDuration);
        }
        return n;
    }

    public static long getUploadsForOwner(User owner){
        String key = getUserKey("uploads");
        Long n = Cache.get(key, Long.class);
        if(n == null){
            n = Upload.countForOwner(owner);
            Cache.add(key, n, UserPropertiesDuration);
        }
        return n;
    }

    public static long getClaims(){
        String key = "claims";
        Long n = Cache.get(key, Long.class);
        if(n == null){
            n = Project.countClaims();
            Cache.add(key, n, UserPropertiesDuration);
        }
        return n;
    }

    public static void evictProjectsForOwner(User user) {
        String key = getUserKey("projects");
        Cache.delete(key);
    }

    public static void evictUploadsForOwner(User user) {
        String key = getUserKey("uploads");
        Cache.delete(key);
    }

    public static void evictClaims() {
        Cache.delete("claims");
    }

	public static long getModulesForOwner(User owner){
		String key = getUserKey("modules");
		Long n = Cache.get(key, Long.class);
		if(n == null){
			n = Module.count();
			Cache.add(key, n, UserPropertiesDuration);
		}
		return n;
	}
}
