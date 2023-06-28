package com.unibeam.passkey1;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {
    public static String readFromAssets(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();

        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream;
        try {
            inputStream = assetManager.open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            return null;
        }

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ignored) {}
        }

        return stringBuilder.toString();
    }
}
