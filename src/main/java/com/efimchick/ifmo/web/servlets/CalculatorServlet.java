package com.efimchick.ifmo.web.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/calc/*")
public class CalculatorServlet extends HttpServlet {

    Map<String, HashMap<String,String>> repository = null;

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String expression = req.getParameter("expression");

        String sessionId = req.getSession().getId();



        addParametersToRepository(sessionId, expression);

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String sessionId = req.getSession().getId();

    }

    private void addParametersToRepository(String sessionId, String parameter) {
        if(repository.get(sessionId).containsKey(parameter)){

        }

    }
}
