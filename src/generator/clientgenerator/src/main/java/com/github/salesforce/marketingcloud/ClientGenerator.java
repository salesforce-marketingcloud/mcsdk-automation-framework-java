package com.github.salesforce.marketingcloud;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientGenerator
{
    public static void main(String[] args ) throws IOException {
        if(args.length == 0)
        {
            System.out.println("Invalid argument. Must pass the full path to the folder containing the generated Api classes");
        }

        String apiClassesFolder = args[0];

        System.out.println("Finding Api classes from folder " + apiClassesFolder);

        Stream<Path> walk = Files.walk(Paths.get(apiClassesFolder));

        List<String> apiClasses = walk
                .filter(Files::isRegularFile)
                .filter(x -> x.toString().endsWith("Api.java") && !x.toString().endsWith("BaseApi.java"))
                .map(x -> x.getFileName().toString().replace(".java", ""))
                .sorted()
                .collect(Collectors.toList());

        System.out.println("Found");
        apiClasses.forEach(System.out::println);

        String generatedClientClassContent = generateClientClass(apiClasses);

        Path clientFilePath = Paths.get(apiClassesFolder, "Client.java");
        System.out.println("Writing the generated Client class to " + clientFilePath);
        Files.write(clientFilePath, generatedClientClassContent.getBytes());
        System.out.println("Done");
    }

    private static String generateApiGetter(String apiType) throws IOException {
        String apiGetterTemplate = readFileContent("/ApiGetter.template");

        apiGetterTemplate = apiGetterTemplate.replace("{{ApiType}}", apiType);

        return apiGetterTemplate.replace("{{fieldName}}", apiType.substring(0, 1).toLowerCase() + apiType.substring(1));
    }

    private static String generateApiGetters(List<String> apiClasses) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String apiClass : apiClasses) {
            sb.append(generateApiGetter(apiClass));
        }
        return sb.toString();
    }

    private static String generateClientClass(List<String> apiClasses) throws IOException {
        String clientTemplate = readFileContent("/Client.template");
        return clientTemplate.replace("{{apiGetters}}", generateApiGetters(apiClasses));
    }

    private static String readFileContent(String templateFilePath) throws IOException {
        InputStream is = ClientGenerator.class.getResourceAsStream(templateFilePath);

        byte[] fileBytes = new byte[is.available()];
        is.read(fileBytes,0, is.available());
        return new String(fileBytes);
    }
}
