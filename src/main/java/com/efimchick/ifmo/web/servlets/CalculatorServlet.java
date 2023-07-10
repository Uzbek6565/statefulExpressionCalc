package com.efimchick.ifmo.web.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/calc/*")
public class CalculatorServlet extends HttpServlet {

    Map<String, HashMap<String,String>> repository = null;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String sessionId = req.getSession().getId();

        System.out.println("in doGet method " + req.getSession().getId());
        List<Integer> closeBracketList = (List<Integer>) req.getSession().getAttribute("closeBracketList");
        List<Integer> openBracketList = (List<Integer>) req.getSession().getAttribute("openBracketList");
        Map<Integer, Integer> closeBracketMap = (Map<Integer, Integer>) req.getSession().getAttribute("closeBracketMap");
        Map<Integer, Character> signMap = (Map<Integer, Character>) req.getSession().getAttribute("signMap");
        Map<Integer, Integer> valueMap = (Map<Integer, Integer>) req.getSession().getAttribute("valueMap");
        String expression = (String) req.getSession().getAttribute("expression");
        if(closeBracketList == null || openBracketList == null || closeBracketMap == null || signMap == null || valueMap == null ){
            resp.setStatus(409);
            resp.getWriter().write("bad request");
            req.getRequestDispatcher("badServer.jsp").forward(req, resp);
        }else {
            int result = 0;

            for (int i = 0; i < closeBracketList.size(); i++) {
                Integer start = openBracketList.get(closeBracketList.size() - 1 - i);
                Integer end = getLastCloseBracket(start, i, closeBracketMap);
                result = doMath(start, end, signMap, valueMap);
                System.out.println(result);
            }

            result = doMath(0, expression.length(), signMap, valueMap);

            resp.setContentType("text/html");
            PrintWriter out = resp.getWriter();
            out.println(result);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("in doPost method");
        String expression = req.getParameter("expression");
        String queryString = req.getQueryString();
        String sessionId = req.getSession().getId();

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("in doDelete method");
    }

    private Integer getLastCloseBracket(Integer start, int i, Map<Integer, Integer> closeBracketMap) {
        int x = 0;
        for (Map.Entry<Integer,Integer> bracket : closeBracketMap.entrySet()) {
            if(bracket.getKey() > start && bracket.getValue() == 1){
                x = bracket.getKey();
                closeBracketMap.put(x, 0);
                break;
            }
        }
        return x;
    }

    private int doMath(Integer start, Integer end, Map<Integer, Character> signMap, Map<Integer, Integer> valueMap) {
        Map<Integer, Integer> signByPriority = new HashMap<>();
        List<Integer> removableSignPosition = new ArrayList<>();
        List<Integer> removableValuesPosition = new ArrayList<>();
        int result = 0;
        int before, after;



        signByPriority = getByPriority(start, end, signMap);

        for (Map.Entry<Integer, Integer> prioritySign : signByPriority.entrySet()) {
            boolean flag = false;
            for (Map.Entry<Integer, Character> sign : signMap.entrySet()) {
                if (prioritySign.getValue() == sign.getKey()){
                    List<Integer> sortedValue = new ArrayList<>(valueMap.keySet());
                    Collections.sort(sortedValue);

                    before = getBefore(sortedValue, sign.getKey(), start, end);
                    after = getAfter(sortedValue, sign.getKey(), start, end);
                    switch (sign.getValue()) {
                        case '*':
                            result = valueMap.get(before) * valueMap.get(after);
                            break;
                        case '/':
                            result = valueMap.get(before) / valueMap.get(after);
                            break;
                        case '+':
                            result = valueMap.get(before) + valueMap.get(after);
                            break;
                        case '-':
                            result = valueMap.get(before) - valueMap.get(after);
                            break;
                    }
                    removableSignPosition.add(sign.getKey());
                    removableValuesPosition.add(after);
                    valueMap.put(before, result);
                    flag = true;
                    break;
                }
            }
            for (Integer signPos : removableSignPosition) {
                signMap.remove(signPos);
            }
            for (Integer remPos : removableValuesPosition) {
                valueMap.remove(remPos);
            }
        }
        return result;
    }

    private Map<Integer, Integer> getByPriority(Integer start, Integer end, Map<Integer, Character> signMap) {
        Map<Integer, Integer> filteredMap = new HashMap<>();
        int i = 0;
        for (Map.Entry<Integer, Character> sign : signMap.entrySet()) {
            if (sign.getKey() > start && sign.getKey() < end){
                switch (sign.getValue()) {
                    case '*':
                    case '/':
                        filteredMap.put(i, sign.getKey());
                        i++;
                        break;
                }
            }
        }
        for (Map.Entry<Integer, Character> sign : signMap.entrySet()) {
            if (sign.getKey() > start && sign.getKey() < end){
                switch (sign.getValue()) {
                    case '+':
                    case '-':
                        filteredMap.put(i, sign.getKey());
                        i++;
                        break;
                }
            }
        }
        return filteredMap;
    }

    private int getAfter(List<Integer> sortedValue, Integer key, Integer start, Integer end) {
        for (Integer position : sortedValue) {
            if (position > key && position > start && position <= end) {
                return position;
            }
        }
        return 0;
    }

    private int getBefore(List<Integer> sortedValue, Integer key, Integer start, Integer end) {
        int before = 0;
        for (Integer position : sortedValue) {
            if (position > key && position >= start && position < end) {
                return before;
            }
            before =  position;
        }
        return 0;
    }

}
