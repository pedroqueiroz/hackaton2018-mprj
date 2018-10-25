<%@ include file="init.jsp" %>

<div id="<portlet:namespace />"></div>

<portlet:resourceURL id="getFormEntriesByUser" var="getFormEntriesByUserURL" />

<aui:script require="<%= bootstrapRequire %>">
	window.getFormEntriesByUser = <%= getFormEntriesByUserURL %>

	bootstrapRequire.default('<portlet:namespace />');
</aui:script>