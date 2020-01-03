package server;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class FriendExclusionStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
        return (fieldAttributes.getDeclaringClass() == User.class &&
                fieldAttributes.getName().equals("hash") ||
                fieldAttributes.getName().equals("userScore") ||
                fieldAttributes.getName().equals("wins") ||
                fieldAttributes.getName().equals("losses") ||
                fieldAttributes.getName().equals("rateo") ||
                fieldAttributes.getName().equals("clientPath") ||
                fieldAttributes.getName().equals("userInfoPath") ||
                fieldAttributes.getName().equals("friendlistPath"));
    }

    @Override
    public boolean shouldSkipClass(Class<?> aClass) {
        return false;
    }
}
