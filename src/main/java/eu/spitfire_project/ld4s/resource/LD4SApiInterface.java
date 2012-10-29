package eu.spitfire_project.ld4s.resource;

import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

public interface LD4SApiInterface {
@Get
public Representation get();

//@Put
//public Representation put(LD4SObject obj);

@Put
public Representation put(Form obj);

@Put
public Representation put(JSONObject obj);

//@Post
//public Representation post(LD4SObject obj);

@Post
public Representation post(Form obj);

@Post
public Representation post(JSONObject obj);

@Delete
public void remove();
}
