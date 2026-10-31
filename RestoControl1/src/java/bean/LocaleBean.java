package bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

@Named("localeBean")
@SessionScoped
public class LocaleBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private String idioma = "es";

    public String getIdioma() {
        Object idiomaSesion = sessionMap().get("idioma");
        if (idiomaSesion != null) {
            idioma = normalizar(idiomaSesion.toString());
        }
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = normalizar(idioma);
        sessionMap().put("idioma", this.idioma);
    }

    public Locale getLocale() {
        return Locale.forLanguageTag(getIdioma());
    }

    private String normalizar(String valor) {
        if ("en".equals(valor) || "it".equals(valor) || "pt".equals(valor)) {
            return valor;
        }
        return "es";
    }

    private Map<String, Object> sessionMap() {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
    }
}
