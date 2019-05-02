package com.infoshare.jjdd6.moviespotter.servlets;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.infoshare.jjdd6.moviespotter.dao.ProgrammeDao;
import com.infoshare.jjdd6.moviespotter.models.Programme;
import com.infoshare.jjdd6.moviespotter.services.FilmWebBrowser;
import com.infoshare.jjdd6.moviespotter.services.ProgrammeAllTitlesList;
import com.infoshare.jjdd6.moviespotter.freemarker.TemplateProvider;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import info.talacha.filmweb.models.Film;
import org.apache.commons.lang3.math.NumberUtils;
import info.talacha.filmweb.search.models.FilmSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(urlPatterns = {"/programme/detail"})
public class ProgrammeDetailsServlet extends HttpServlet {

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @Inject
    ProgrammeDao programmeDao;

    @Inject
    FilmWebBrowser filmWebBrowser;

    @Inject
    ProgrammeAllTitlesList programmeAllTitlesList;

    @Inject
    TemplateProvider templateProvider;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        int id = (NumberUtils.toInt(request.getParameter("id"), 0));

        Programme detailed = programmeDao.findById(id);

        List<String> allTitles = (programmeAllTitlesList.getTitlesList(id));

        List<FilmSearchResult> briefList = filmWebBrowser.findMoviesBriefInfo(id);

        Map<String, Object> model = new HashMap<>();

        for (FilmSearchResult filmSearchResult : briefList) {

            for (String allTitle : allTitles) {

                if ((allTitle.equalsIgnoreCase(filmSearchResult.getTitle())
                        || allTitle.equalsIgnoreCase(filmSearchResult.getPolishTitle())
                        || allTitle.equalsIgnoreCase(filmSearchResult.getAlternativeTitle()))
                        && detailed.getDate() == filmSearchResult.getYear()
                ) {
                    Film film = filmWebBrowser.getFilmInfo(filmSearchResult.getId());

                    if (film != null) {
                        model.put("m_movie", film);
                        model.put("m_persons", filmWebBrowser.getFilmPersons(filmSearchResult.getId()));
                        break;
                    }
                }
            }
        }

        model.put("movies", briefList);

        Template template = templateProvider.getTemplate(getServletContext(), "movieDetails.ftlh");

        try {
            template.process(model, response.getWriter());
        } catch (TemplateException e) {
            log.error("Error processing template: "+e);
        }
    }
}
