<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" session="false" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<html lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">

<script src="https://cdn.signalfx.com/o11y-gdi-rum/v0.17.0/splunk-otel-web.js" crossorigin="anonymous"></script>
<script>
    SplunkRum.init({
        realm: "us1",
        rumAccessToken: "SY8ZqV1LHQOTVKxSE7i9Dg",
        applicationName: "teastoreOnline",
        deploymentEnvironment: "lab"
    });
</script>
<script src="https://cdn.signalfx.com/o11y-gdi-rum/v0.17.0/splunk-otel-web-session-recorder.js" crossorigin="anonymous"></script>
    <script>
    SplunkSessionRecorder.init({
      app: "teastoreOnline",
      realm: "us1",
      rumAccessToken: "SY8ZqV1LHQOTVKxSE7i9Dg"
    });
    </script>
<link rel="icon" href="<c:url value="/images/icon.ico" />" >
<title>${title}</title>
<%-- Bootstrap core CSS --%>
<link rel="stylesheet" href="<c:url value="bootstrap/css/bootstrap.min.css"/>" type="text/css" />
<link rel="StyleSheet" href="<c:url value="/teastore.css"/>" type="text/css" media="screen" />
</head>
<body>
