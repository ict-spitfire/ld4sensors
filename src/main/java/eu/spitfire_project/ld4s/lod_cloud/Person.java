package eu.spitfire_project.ld4s.lod_cloud;

public class Person {

	private String firstname = null;

	private String uri = null;

	private String surname = null;

	private String nickname = null;

	private String email = null;

	private String homepage = null;

	private String weblog = null;

	public Person(){
		new Person(null, null, null, null, null, null, null);
	}
	public Person(String firstname, String surname, String nickname, String email,
			String homepage, String weblog, String uri){
		setFirstname(firstname);
		setSurname(surname);
		setNickname(nickname);
		setEmail(email);
		setHomepage(homepage);
		setWeblog(weblog);
		setUri(uri);
	}

	public void setFirstname(String firstname) {
		if (firstname != null){
			this.firstname = Character.toUpperCase(firstname.charAt(0))+firstname.substring(1);
		}
	}

	public String getFirstname() {
		return firstname;
	}

	public void setSurname(String surname) {
		if (surname != null){
			this.surname = Character.toUpperCase(surname.charAt(0))+surname.substring(1);
		}
	}

	public String getSurname() {
		return surname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getNickname() {
		return nickname;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public String getHomepage() {
		return homepage;
	}

	public void setWeblog(String weblog) {
		this.weblog = weblog;
	}

	public String getWeblog() {
		return weblog;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getUri() {
		return uri;
	}


}
