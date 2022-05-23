<#-- @ftlvariable name="" type="io.dropwizard.documentation.ErrorView" -->
<html lang="en">
<body>
<h1>Error: ${errorMessage.code?html}</h1>
<h2>${errorMessage.message?html}</h2>
<p>${errorMessage.details?html}</p>
</body>
</html>