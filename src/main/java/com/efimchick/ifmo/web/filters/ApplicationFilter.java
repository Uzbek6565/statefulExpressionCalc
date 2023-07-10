package com.efimchick.ifmo.web.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Character.*;

@WebFilter(urlPatterns = "/calc/*")
public class ApplicationFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("in doFilter method");

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        Set<Character> parameterSet = (Set<Character>) request.getSession().getAttribute("parameterSet");
        List<Integer> openBracketList = (List<Integer>) request.getSession().getAttribute("openBracketList");
        List<Integer> closeBracketList = (List<Integer>) request.getSession().getAttribute("closeBracketList");
        Map<Integer, Integer> closeBracketMap = (Map<Integer, Integer>) request.getSession().getAttribute("closeBracketMap");

        Map<Integer, Character> expressionMap = (Map<Integer, Character>) request.getSession().getAttribute("expressionMap");
        Map<Integer, Integer> valueMap = (Map<Integer, Integer>) request.getSession().getAttribute("valueMap");
        Map<Integer, Character> parameterMap = (Map<Integer, Character>) request.getSession().getAttribute("parameterMap");
        Map<Integer, Character> signMap = (Map<Integer, Character>) request.getSession().getAttribute("signMap");

        Map<String, String> repository = (Map<String, String>) request.getSession().getAttribute("repository");

        if (parameterSet == null) {
            parameterSet = new HashSet<>();
        }
        if (openBracketList == null) {
            openBracketList = new ArrayList<>();
        }
        if (closeBracketList == null) {
            closeBracketList = new ArrayList<>();
        }
        if (expressionMap == null) {
            expressionMap = new HashMap<>();
        }
        if (closeBracketMap == null) {
            closeBracketMap = new HashMap<>();
        }if (valueMap == null) {
            valueMap = new HashMap<>();
        }if (parameterMap == null) {
            parameterMap = new HashMap<>();
        }if (signMap == null) {
            signMap = new HashMap<>();
        }
        if (repository == null) {
            repository = new HashMap<>();
        }

        if (request.getMethod().equals("PUT")) {
            //get parameters after / in the URL
            String parameter = request.getPathInfo().substring(1);

            //get body from request
            String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

            //may be add body != null condition
            if (parameter.equals("expression") && containsSigns(body) && allLowercase(body)) {
                String expression = body.replaceAll("\\s", "");
                if (!repository.containsKey(request.getSession().getId())) {
                    repository.put(request.getSession().getId(), expression);
                    request.getSession().setAttribute("repository", repository);
                    response.setStatus(201);
                } else {
                    response.setStatus(200);
                }
                System.out.println("Size of repo " + repository.size());
                System.out.println(expression);
                for (int i = 0; i < expression.length(); i++) {
                    char character = expression.charAt(i);
                    expressionMap.put(i, character);
                    if (character == '(') {
                        openBracketList.add(i);
                    }
                    if (character == ')') {
                        closeBracketMap.put(i, 1);
                        closeBracketList.add(i);
                    }
                    if (character == '+' || character == '-' || character == '*' || character == '/') {
                        signMap.put(i, character);
                    }
                    if (Character.isAlphabetic(expression.charAt(i))) {
                        parameterSet.add(character);
                        parameterMap.put(i, character);
                    }
                }
                if (openBracketList.size() != closeBracketList.size()) {
                    response.setStatus(400);
                    response.getWriter().write("Error");
                    request.getRequestDispatcher("/badServer.jsp").forward(servletRequest, servletResponse);
                } else {
                    request.getSession().setAttribute("expression", expression);
                    request.getSession().setAttribute("openBracketList", openBracketList);
                    request.getSession().setAttribute("closeBracketList", closeBracketList);
                    request.getSession().setAttribute("closeBracketMap", closeBracketMap);
                    request.getSession().setAttribute("parameterSet",parameterSet);
                    request.getSession().setAttribute("signMap", signMap);
                    request.getSession().setAttribute("parameterMap", parameterMap);
                    request.getSession().setAttribute("expressionMap", expressionMap);

                }

            }
            else if (!parameter.equals("expression") && isLowerCase(parameter.charAt(0)) && parameter.length() == 1) {
                if (isParameterHasOverLimitValue(body)) {
                    response.setStatus(403);
                    response.getWriter().write("Error");
                } else {
                    System.out.println("Value is not more than 10000");
                    for (Character character : parameterSet) {
                        Integer value = 0;
                        String params = body;
                        if (isDigit(params.charAt(0))) {
                            value = Integer.valueOf(params);
                        } else {
                            value = getValueOfParameter(params, parameterMap, valueMap);
                        }

                        for (Map.Entry<Integer, Character> param : parameterMap.entrySet()) {
                            if (param.getValue() == character && character == parameter.charAt(0)) {
                                valueMap.put(param.getKey(), value);

                            }
                        }
                    }
                    request.getSession().setAttribute("valueMap", valueMap);
                    if (!repository.containsKey(parameter)) {
                        repository.put(parameter, parameter);
                        request.getSession().setAttribute("repository", repository);
                        response.setStatus(201);
                    }else {
                        response.setStatus(200);
                    }
                }
            } else {
                response.setStatus(400);
                response.getWriter().write("Error");
                request.getRequestDispatcher("/badServer.jsp").forward(servletRequest, servletResponse);
            }
        }
        else if (request.getMethod().equals("GET")) {
            boolean flag = false;
            for (Map.Entry<Integer, Character> param: parameterMap.entrySet()) {
                if (!parameterSet.contains(param.getValue())) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                System.out.println("Not enough parameter");
                response.setStatus(409);
                response.getWriter().write("Not enough parameter");
                request.getRequestDispatcher("/badServer.jsp").forward(servletRequest, servletResponse);
            }
        } else if (request.getMethod().equals("DELETE")) {
            System.out.println("Deleting Session " + request.getSession().getId());
            repository.remove(request.getSession().getId());
            response.setStatus(204);
        }
        filterChain.doFilter(request,servletResponse);
    }

    private Integer getValueOfParameter(String parameter, Map<Integer, Character> parameterMap, Map<Integer, Integer> valueMap) {
        for (Map.Entry<Integer, Character> param : parameterMap.entrySet()) {
            if (param.getValue() == parameter.charAt(0)) {
                return valueMap.get(param.getKey());
            }
        }
        return 0;
    }


    private boolean allLowercase(String body) {
        for (int i = 0; i < body.length(); i++) {
            if (!isLowerCase(body.charAt(i)) && isAlphabetic(body.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean containsSigns(String body) {
        if (body.contains("+") || body.contains("-") || body.contains("/") || body.contains("*")) {
            return true;
        }
        return false;
    }


    private boolean isParameterHasOverLimitValue(String paramValue) {
        return isNumeric(paramValue) && Math.abs(Integer.parseInt(paramValue)) > 10000;
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
