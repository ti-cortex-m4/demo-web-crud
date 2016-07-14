package org.couchbase.devex;

import com.google.common.collect.ImmutableMap;
import org.couchbase.devex.domain.StoredFileRenderer;
import org.couchbase.devex.service.SearchService;
import org.springframework.context.annotation.Configuration;
import ratpack.form.Form;
import ratpack.func.Action;
import ratpack.guice.BindingsSpec;
import ratpack.handlebars.HandlebarsModule;
import ratpack.handlebars.Template;
import ratpack.handling.Chain;
import ratpack.rx.RxRatpack;
import ratpack.server.BaseDir;
import ratpack.server.ServerConfigBuilder;
import ratpack.spring.config.RatpackServerCustomizer;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class RatpackConfiguration implements RatpackServerCustomizer {

    @Override
    public List<Action<Chain>> getHandlers() {
        List<Action<Chain>> handlers = new ArrayList<Action<Chain>>();
        handlers.add(fileApi());
        return handlers;
    }

    @Override
    public Action<ServerConfigBuilder> getServerConfig() {
        return config -> config.baseDir(BaseDir.find())
                .props(ImmutableMap.of("server.maxContentLength", "100000000", "app.name", "Search Store File"));

    }

    @Override
    public Action<BindingsSpec> getBindings() {
        return bindingConfig -> bindingConfig
                .module(HandlebarsModule.class)
                .bind(FileHandler.class)
                .bind(StoredFileRenderer.class)
                .bind(ErrorHandlerImpl.class)
                .bind(ClientHandlerImpl.class);
    }

    private Action<Chain> fileApi() {
        return chain -> chain.prefix("file", FileHandler.class).post("fulltext", ctx -> {
            ctx.parse(Form.class).then(form -> {
                String queryString = form.get("queryString");
                SearchService searchService = ctx.get(SearchService.class);
                Observable<Map<String, Object>> files = searchService.searchFulltextFiles(queryString);
                RxRatpack.promise(files).then(response -> ctx
                        .render(Template.handlebarsTemplate("uploadForm", "text/html", m -> m.put("files", response))));
            });
        }).post("n1ql", ctx -> {
            ctx.parse(Form.class).then(form -> {
                String queryString = form.get("queryString");
                SearchService searchService = ctx.get(SearchService.class);
                Observable<Map<String, Object>> files = searchService.searchN1QLFiles(queryString);
                RxRatpack.promise(files).then(response -> ctx
                        .render(Template.handlebarsTemplate("uploadForm", "text/html", m -> m.put("files", response))));
            });
        });
    }
}