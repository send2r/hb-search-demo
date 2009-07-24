/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.demo.model.Song;
import org.demo.ui.DemoUI;
import org.hibernate.Session;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.jpa.FullTextEntityManager;

/**
 *
 * @author RanjiRam
 */
public class Application {

    private static EntityManagerFactory entityManagerFactory;
    private static final Log LOG = LogFactory.getLog(Application.class);
    private static String[] songFields = {"title", "artist", "album", "notes"};

    public static void main(String[] args) throws ParseException {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    final DemoUI ui = new DemoUI();
                    ui.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        if(false){
        LOG.info("Main started");
        Ejb3Configuration configuration = new Ejb3Configuration();
        configuration.configure("music-db", new HashMap(1));
        entityManagerFactory = configuration.buildEntityManagerFactory();
        LOG.info("entityManagerFactory init complete");
        Application demoApp = new Application();
        demoApp.buildIndex();
        //all small case,
        LOG.info("-----------------------");
        LOG.info("Testing Wild card Query");
        LOG.info("-----------------------");
        printQueryResult(demoApp.createHbWildCardQuery("b*ll"));
        LOG.info("------------------------");
        LOG.info("Testing MultiField query");
        LOG.info("------------------------");
        //Title has ticket or any field contains text LA
        printQueryResult(demoApp.createHbMultiFieldSearchQuery("title: \"Ticket\" \"LA\""));
        LOG.info("------------------");
        LOG.info("Testing Term Query");
        LOG.info("------------------");
        printQueryResult(demoApp.createHbTermQuery("artist", "the beatles"));
        LOG.info("-----------------------");
        LOG.info("Testing Match-all Query");
        LOG.info("-----------------------");
        printQueryResult(demoApp.createHbMatchAllQuery());
        LOG.info("-----------------------");
        LOG.info("Testing Prefix Query");
        LOG.info("-----------------------");
        printQueryResult(demoApp.createHbPrefixQuery("random"));
        LOG.info("-----------------------");
        LOG.info("Testing Fuzzy Query");
        LOG.info("-----------------------");
        printQueryResult(demoApp.createHbFuzzyQuery("builder"));
        }
    }

    private static void printQueryResult(Query q) {
        List<Song> songs = q.getResultList();
        LOG.info("Found " + songs.size() + " Results.");
        for (Song s : songs) {
            LOG.info(s);
        }
    }

    private void buildIndex() {
        EntityManager manager = entityManagerFactory.createEntityManager();
        try {
            manager.getTransaction().begin();
            //Hibernate search in action. All classes below are from hibernate
            FullTextSession fullTextSession = Search.getFullTextSession((Session) manager.getDelegate());
            List songObjList = fullTextSession.createCriteria(Song.class).list();
            LOG.info("Building index");
            //manually building our index
            for (Object songObject : songObjList) {
                fullTextSession.index(songObject);
            }
        } finally {
            manager.getTransaction().commit();
            manager.close();
            LOG.info("Built index");
        }
    }

    private Query createHbFuzzyQuery(String userInput) {
        FuzzyQuery fq = new FuzzyQuery(new Term("album", userInput), 0.4F);
        EntityManager em = entityManagerFactory.createEntityManager();
        FullTextEntityManager ftEm = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
        return ftEm.createFullTextQuery(fq, Song.class);
    }

    private Query createHbPrefixQuery(String userInput) {
        org.apache.lucene.search.Query query = new WildcardQuery(new Term("notes", userInput));
        EntityManager em = entityManagerFactory.createEntityManager();
        FullTextEntityManager ftEm = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
        return ftEm.createFullTextQuery(query, Song.class);
    }

    private Query createHbTermQuery(String searchField, String searchText) {
        String[] toks = searchText.split("\\s");
        BooleanQuery booleanQuery = new BooleanQuery();
        for (String tok : toks) {
            org.apache.lucene.search.Query termQuery = new TermQuery(new Term(searchField, tok));
            booleanQuery.add(termQuery, BooleanClause.Occur.SHOULD);
        }

        EntityManager manager = entityManagerFactory.createEntityManager();
        FullTextEntityManager ftem = org.hibernate.search.jpa.Search.getFullTextEntityManager(manager);
        return ftem.createFullTextQuery(booleanQuery, Song.class);
    }

    private Query createHbMultiFieldSearchQuery(String searchText) throws ParseException {
        Map<String, Float> weightMap = new HashMap<String, Float>(1);
        weightMap.put("title", 5F);
        weightMap.put("artist", 5F);
        weightMap.put("album", 5F);
        weightMap.put("notes", 5F);
        QueryParser queryParser = new MultiFieldQueryParser(songFields, new StandardAnalyzer(), weightMap);
        queryParser.setDefaultOperator(QueryParser.OR_OPERATOR);
        org.apache.lucene.search.Query lQuery = queryParser.parse(searchText);
        EntityManager manager = entityManagerFactory.createEntityManager();
        FullTextEntityManager ftem = org.hibernate.search.jpa.Search.getFullTextEntityManager(manager);
        return ftem.createFullTextQuery(lQuery, Song.class);
    }

    private Query createHbMatchAllQuery() {
        org.apache.lucene.search.Query query = new MatchAllDocsQuery();
        EntityManager em = entityManagerFactory.createEntityManager();
        FullTextEntityManager ftEm = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
        return ftEm.createFullTextQuery(query, Song.class);
    }

    private Query createHbWildCardQuery(String wildCard) {
        org.apache.lucene.search.Query query = new WildcardQuery(new Term("title", wildCard));
        EntityManager em = entityManagerFactory.createEntityManager();
        FullTextEntityManager ftEm = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
        return ftEm.createFullTextQuery(query, Song.class);
    }
}