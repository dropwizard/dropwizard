<#-- @ftlvariable name="" type="com.example.helloworld.views.GuessMyNumberView" -->
<html>
<body>
    <h1>Guess which number I'm thinking of</h1>

    <form action="/guess/number" method="POST" name="guessNumberForm">
        <strong>${message}</strong><br />

        <label for="number">Number: </label>
        <input type="text" name="number" /><br />
        <input type="submit" value="Go"/>
    </form>
</body>
</html>