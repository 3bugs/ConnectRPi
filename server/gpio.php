<html>
<head>
    <meta name="viewport" content="width=device-width" />
    <title>LED Control</title>
</head>
<body style="padding: 20px; ">
<h2>LED Control</h2>

<form method="get" action="<?php echo $_SERVER['PHP_SELF']; ?>">
    <table border="0" cellpadding="6px" cellspacing="0px">
        <tr>
            <th>Turn On LED #<br><img src="images/ic_light_on.png" width="30px" height="30px"></th>
            <th>&nbsp;</th>
            <th>Turn Off LED #<br><img src="images/ic_light_off.png" width="30px" height="30px"></th>
        </tr>
    <?php
    $pinArray = array(18, 23, 12, 16);

    foreach ($pinArray as $pin) {
        $tags = <<<EOT
        <tr>
            <td align="center">
                <input style="padding: 6px; width: 80px; " type="submit" value="$pin" name="on">
             </td>
            <td>&nbsp;</td>
            <td align="center">
                <input style="padding: 6px; width: 80px; " type="submit" value="$pin" name="off">
             </td>
        </tr>
EOT;
        echo $tags;
    }
    ?>
    </table>
</form>

<?php
if (isset($_GET['on'])) {
    $pinNumber = $_GET['on'];
} else if (isset($_GET['off'])) {
    $pinNumber = $_GET['off'];
}

$setMode = shell_exec("/usr/local/bin/gpio -g mode $pinNumber out");
if (isset($_GET['on'])) {
    shell_exec("/usr/local/bin/gpio -g write $pinNumber 1");
    //echo "LED is on";
} else if (isset($_GET['off'])) {
    shell_exec("/usr/local/bin/gpio -g write $pinNumber 0");
    //echo "LED is off";
}

//$output = shell_exec("/usr/local/bin/gpio -g read 18");
//echo $output;
?>

</body>
</html>
