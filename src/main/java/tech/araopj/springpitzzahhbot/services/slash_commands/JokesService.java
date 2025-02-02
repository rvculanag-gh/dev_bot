/*
 * MIT License
 *
 * Copyright (c) 2022 pitzzahh
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package tech.araopj.springpitzzahhbot.services.slash_commands;

import tech.araopj.springpitzzahhbot.entities.approve_joke.Joke;
import tech.araopj.springpitzzahhbot.entities.get_joke.Category;
import tech.araopj.springpitzzahhbot.entities.get_joke.Language;
import tech.araopj.springpitzzahhbot.services.configs.SecretService;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import com.fasterxml.jackson.core.JsonProcessingException;
import tech.araopj.springpitzzahhbot.configs.HttpConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Service;
import com.google.gson.reflect.TypeToken;
import java.util.stream.Collectors;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import java.util.Collection;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.net.URI;

@Slf4j
@Service
public record JokesService(
        SecretService secretService,
        HttpConfig httpConfig
) {

    public Collection<Category> getCategories() {
        var httpResponseCompletableFuture = httpConfig.httpClient()
                .sendAsync(HttpRequest.newBuilder()
                                .GET()
                                .uri(URI.create("https://jokes.araopj.tech/v1/resource/categories"))
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                );
        HttpResponse<String> stringHttpResponse;
        try {
            stringHttpResponse = httpResponseCompletableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while getting categories", e);
            throw new RuntimeException(e);
        }

        var categories = (String[]) new Gson().fromJson(stringHttpResponse.body(), new TypeToken<String[]>() {
        }.getType());

        // Now the categories array should contain the parsed values
        log.info("Categories: {}", Arrays.toString(categories));
        return Arrays.stream(categories)
                .map(category -> new Category(category, category))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Collection<Language> getLanguages() {
        var httpResponseCompletableFuture = httpConfig.httpClient()
                .sendAsync(HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create("https://jokes.araopj.tech/v1/resource/languages"))
                        .build(), HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> stringHttpResponse;
        try {
            stringHttpResponse = httpResponseCompletableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while getting languages", e);
            throw new RuntimeException(e);
        }

        var languages = (String[]) new Gson().fromJson(stringHttpResponse.body(), new TypeToken<String[]>() {
        }.getType());

        // Now the categories array should contain the parsed values
        log.info("Languages: {}", Arrays.toString(languages));

        return Arrays.stream(languages)
                .map(language -> new Language(language, language))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public String createJokeRequestUrl(OptionMapping category, OptionMapping language) {
        var url = httpConfig.getJokeApiUrl();
        if (category != null && language != null)
            url += "random?category=" + category.getAsString() + "&language=" + language.getAsString();
        else if (category != null) url += "random?category=" + category.getAsString();
        else if (language != null) url += "random?language=" + language.getAsString();
        else url += "random";
        return url;
    }

    public String createJokeSubmitUrl() {
        return httpConfig.getJokeApiUrl().concat("submit");
    }

    public String createJokeSubmitBody(OptionMapping joke, OptionMapping category, OptionMapping language) {
        var jokeObject = category != null ? Joke.builder()
                .joke(joke.getAsString())
                .category(category.getAsString())
                .language(language.getAsString())
                .build() : Joke.builder()
                .joke(joke.getAsString())
                .language(language.getAsString())
                .build();
        return new Gson().toJson(jokeObject);
    }

    public Collection<Joke> getSubmittedJokes() {
        var httpResponseCompletableFuture = httpConfig.httpClient()
                .sendAsync(HttpRequest.newBuilder()
                                .GET()
                                .uri(URI.create("%s/all".formatted(createJokeSubmitUrl())))
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                );
        HttpResponse<String> stringHttpResponse;
        try {
            stringHttpResponse = httpResponseCompletableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while getting categories", e);
            throw new RuntimeException(e);
        }
        var objectMapper = new ObjectMapper();
        Collection<Joke> jokes;
        try {
            jokes = objectMapper.readValue(stringHttpResponse.body(), new TypeReference<>() {
            });
            log.info("Submitted Jokes: {}", jokes);
        } catch (JsonProcessingException e) {
            log.error("Error while getting categories", e);
            throw new RuntimeException(e);
        }
        return jokes
                .stream()
                .filter(j -> !j.approved())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public boolean approveJoke(Joke joke) {
        var httpResponseCompletableFuture = httpConfig.httpClient()
                .sendAsync(HttpRequest.newBuilder()
                                .uri(URI.create("%s/approve?key=%s&joke_id=%s".formatted(createJokeSubmitUrl(), secretService.getKey(), joke.id())))
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                );
        HttpResponse<String> stringHttpResponse;

        try {
            stringHttpResponse = httpResponseCompletableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while approving joke", e);
            throw new RuntimeException(e);
        }

        log.info("Approve Joke Response: {}", stringHttpResponse.body());
        return stringHttpResponse.body().equals("Joke approved successfully");
    }
}
