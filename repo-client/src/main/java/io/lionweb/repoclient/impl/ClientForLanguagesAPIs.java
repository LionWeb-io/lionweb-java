package io.lionweb.repoclient.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.repoclient.RequestFailureException;
import io.lionweb.repoclient.api.HistoryAPIClient;
import io.lionweb.repoclient.api.LanguagesAPIClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientForLanguagesAPIs extends LionWebRepoClientImplHelper implements LanguagesAPIClient {

  public ClientForLanguagesAPIs(RepoClientConfiguration repoClientConfiguration) {
    super(repoClientConfiguration);
  }

    @Override
    public void registerLanguages(Collection<@NotNull Language> languages) {
        throw new UnsupportedOperationException();
    }
}
