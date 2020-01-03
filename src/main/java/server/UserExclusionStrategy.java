package server;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class UserExclusionStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
        return (fieldAttributes.getDeclaringClass() == User.class &&
                (fieldAttributes.getName().equals("userInfoPath") ||
                        fieldAttributes.getName().equals("friendlistPath") ||
                        fieldAttributes.getName().equals("online")
                )
        );
    }

    @Override
    public boolean shouldSkipClass(Class<?> aClass) {
        return false;
    }
}
