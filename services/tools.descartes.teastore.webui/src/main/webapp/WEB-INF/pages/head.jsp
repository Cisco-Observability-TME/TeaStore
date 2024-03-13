<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" session="false" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<html lang="en">
<head>
    <script>
        window.adrum||(function(d) {
            var o=adrum=function(){ o.api.push(arguments)},h=d.getElementsByTagName('head')[0];
            var c=d.createElement('script');o.api=new Array();c.async=true;c.type='text/javascript';
            c.charset='utf-8';c.src='https://cdn.appdynamics.com/adrum-otel/latest/adrum.js';h.appendChild(c);
        })(document);
        adrum('init', {
            appName: 'teastore-webui',
            url: 'https://fso-dpp.observe.appdynamics.com',
            sessionReplay: {
                maskAllText: true, 
                maskAllInputs: true,
                sensitivityRules: [
                    { selector: '.appd-masked', rule: 'mask' },
                    { selector: '.appd-unmasked', rule: 'unmask'},
                    { selector: '.appd-excluded', rule: 'exclude' },
                    
                ],
            },
        });
    </script>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">
<link rel="icon" href="<c:url value="/images/icon.ico" />" >
<title>${title}</title>
<%-- Bootstrap core CSS --%>
<link rel="stylesheet" href="<c:url value="bootstrap/css/bootstrap.min.css"/>" type="text/css" />
<link rel="StyleSheet" href="<c:url value="/teastore.css"/>" type="text/css" media="screen" />
</head>
<body>
