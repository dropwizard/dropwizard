<#-- @ftlvariable name="" type="io.dropwizard.documentation.PersonView" -->
<html lang="en">
<body>
<!-- calls getPerson().getName() and sanitizes it -->
<h1>Hello, ${person.name?html}!</h1>
</body>
</html>