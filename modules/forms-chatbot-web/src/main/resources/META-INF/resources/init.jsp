<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %><%@
taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %><%@
taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %><%@
taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@ page import="forms.chatbot.web.constants.FormsChatbotWebWebKeys" %>

<liferay-theme:defineObjects />

<portlet:defineObjects />

<%
	long formInstanceId = 37408;
%>

<portlet:resourceURL id="getFormEntriesByUser" var="getFormEntriesByUserURL">
	<portlet:param name="formInstanceId" value="<%= String.valueOf(formInstanceId) %>" />
</portlet:resourceURL>

<portlet:resourceURL id="getFormDefinition" var="getFormDefinition">
	<portlet:param name="formInstanceId" value="<%= String.valueOf(formInstanceId) %>" />
</portlet:resourceURL>

<portlet:resourceURL id="saveFormEntry" var="saveFormEntry">
	<portlet:param name="formInstanceId" value="<%= String.valueOf(formInstanceId) %>" />
</portlet:resourceURL>

<script type="text/javascript">
	window.chatBotConstants = {
		getFormEntriesByUserURL: "<%= getFormEntriesByUserURL %>",
		getFormDefinition: "<%= getFormDefinition %>",
		saveFormEntry: "<%= saveFormEntry %>"
	}
</script>

<%
String bootstrapRequire = (String)renderRequest.getAttribute(FormsChatbotWebWebKeys.BOOTSTRAP_REQUIRE);
%>