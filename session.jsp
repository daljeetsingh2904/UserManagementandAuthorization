<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
	System.out.println("Session Values -->"+session.getAttribute("userType")+"-->"+session.getAttribute("userName"));
    if(null==session.getAttribute("userType") || null==session.getAttribute("userName")){
        session.invalidate();
    %>
    <jsp:forward page="index.jsp"/>
    <%
    }
    %>