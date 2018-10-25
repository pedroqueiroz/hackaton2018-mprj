<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %><%@
taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %><%@
taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %><%@
taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@ page import="forms.chatbot.web.constants.FormsChatbotWebWebKeys" %>

<liferay-theme:defineObjects />

<portlet:defineObjects />

<portlet:resourceURL id="getFormEntriesByUser" var="getFormEntriesByUserURL" />

<script type="text/javascript">
    window.getFormEntriesByUserURL = "<%= getFormEntriesByUserURL %>"
</script>

<%
String bootstrapRequire = (String)renderRequest.getAttribute(FormsChatbotWebWebKeys.BOOTSTRAP_REQUIRE);
%>
