package de.jriehn;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.resource.PackageResourceStream;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.agilecoders.wicket.mustache.IScope;
import de.agilecoders.wicket.mustache.WicketMustache.ScopedMap;
import de.agilecoders.wicket.mustache.markup.html.MustachePanel;

public class HomePage extends WebPage {

	@Override
    protected void onInitialize() {
        super.onInitialize();
        IModel<IScope> scopeModel = new LoadableDetachableModel<IScope>() {
            @Override
            public ScopedMap<String, Object> load() {
                IResourceStream steam = new PackageResourceStream(HomePage.class, "mustache.data");
                try {
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(steam.getInputStream(), writer, "UTF-8");
                    return getModelFromJson(new JSONObject(writer.toString()));
                } catch (ResourceStreamNotFoundException e) {
                    throw new WicketRuntimeException(e);
                } catch (IOException e) {
                    throw new WicketRuntimeException(e);
                } finally {
                    try {
                        steam.close();
                    } catch (IOException e) {
                        throw new WicketRuntimeException(e);
                    }
                }
            }
        };

        add(new MustachePanel("template", scopeModel) {
            @Override
            protected IResourceStream newTemplateResourceStream() {
                return new PackageResourceStream(HomePage.class, "mustache.html");
            }
        });
    }

    private ScopedMap<String, Object> getModelFromJson(JSONObject json) throws JSONException {
        Map<String, Object> out = new HashMap<String, Object>();

        Iterator it = json.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (json.get(key) instanceof JSONArray) {
                // Copy an array
                JSONArray arrayIn = json.getJSONArray(key);
                List<Object> arrayOut = new ArrayList<Object>();
                for (int i = 0; i < arrayIn.length(); i++) {
                    JSONObject item = (JSONObject) arrayIn.get(i);
                    Map<String, Object> items = getModelFromJson(item);
                    arrayOut.add(items);
                }
                out.put(key, arrayOut);
            } else {
                // Copy a primitive string
                out.put(key, json.getString(key));
            }
        }

        return new ScopedMap(out);
    }
}
