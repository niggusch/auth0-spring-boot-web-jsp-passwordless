<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>Login</title>
    <script src="http://code.jquery.com/jquery.js"></script>
    <script src="http://cdn.auth0.com/js/lock-passwordless-2.2.min.js"></script>
    <script src="/js/jquery.growl.js" type="text/javascript"></script>
    <link rel="stylesheet" type="text/css" href="/css/bootstrap.css"/>
    <link rel="stylesheet" type="text/css" href="/css/signin.css"/>
    <link rel="stylesheet" type="text/css" href="/css/jquery.growl.css"/>
</head>
<body>

<div class="container">

    <script type="text/javascript">

        $(function () {
            var error = ${error};
            if (error) {
                $.growl.error({message: "An error was detected. Please log in"});
            } else {
                $.growl({title: "Welcome!", message: "Please log in"});
            }
        });

        $(function () {
            setTimeout(function () {
                var lock = new Auth0LockPasswordless('${clientId}', '${domain}');
                lock.magiclink({
                    authParams: {
                        state: '${state}'
                    },
                    responseType: 'code',
                    callbackURL: '${fn:replace(pageContext.request.requestURL, pageContext.request.requestURI, '')}' + '/mcallback'
                }, function (err, email) {
                    if (err) {
                        growl.error({message: 'Error sending e-mail: ' + err.error_description});
                        return;
                    }
                    $('.enter-email').hide();
                    $.growl.notice({message: "Email sent to " + email});
                });
            }, 2000);
        });

    </script>

</div>
</body>
</html>
