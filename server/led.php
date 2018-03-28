<?php

error_reporting(E_ERROR | E_PARSE);
header('Content-type: application/json; charset=utf-8');

header('Expires: Sun, 01 Jan 2014 00:00:00 GMT');
header('Cache-Control: no-store, no-cache, must-revalidate');
header('Cache-Control: post-check=0, pre-check=0', FALSE);
header('Pragma: no-cache');

define('KEY_ERROR_CODE', 'error_code');
define('KEY_ERROR_MESSAGE', 'error_message');
define('KEY_ERROR_MESSAGE_MORE', 'error_message_more');
define('KEY_DATA_LIST', 'data_list');

define('ERROR_CODE_SUCCESS', 0);
define('ERROR_CODE_PARAM_MISSING', 1);
define('ERROR_CODE_INVALID_ACTION', 2);

define('BASE_COMMAND', '/usr/local/bin/gpio -g ');
$response = array();

$request = explode('/', trim($_SERVER['PATH_INFO'], '/'));
$action = strtolower(array_shift($request));
$id = array_shift($request);

switch ($action) {
    case 'turn_on':
        output($id, 1);

        if (isset($_POST['interval'])) {
            $interval = (int) $_POST['interval'];

            if ($interval > 0) {
                $date = date_create();
                date_add($date, date_interval_create_from_date_string("$interval minutes"));
                $turnOffDateTime = date_format($date, 'H:i Y-m-d');

                $taskPhpContent = <<<EOT
<?php
use Crunz\Schedule;
\$schedule = new Schedule();
\$schedule->run('wget http://localhost/app1/led.php/turn_off/$id')
->at('$turnOffDateTime');
return \$schedule;
?>
EOT;
                $taskFile = fopen('./tasks/TurnOffPin' . trim($id) . 'Tasks.php', 'w') or die('Unable to open file!');
                fwrite($taskFile, $taskPhpContent);
                fclose($taskFile);
            }
        }

        $response[KEY_ERROR_CODE] = ERROR_CODE_SUCCESS;
        //$response[KEY_DATA_LIST] = array();

        $pinState = input($id);
        $response['current_state'] = $pinState;

        //$item = array();
        //$item['pin_number'] = (int) $id;
        //$item['state'] = (int) trim($pinState);

        //array_push($response[KEY_DATA_LIST], $item);
        break;

    case 'turn_off':
        output($id, 0);
        $response[KEY_ERROR_CODE] = ERROR_CODE_SUCCESS;
        break;

    case 'get_state':
        $response[KEY_ERROR_CODE] = ERROR_CODE_SUCCESS;
        $response[KEY_DATA_LIST] = array();

        if (isset($id)) {
            $item = array();
            $item['pin_number'] = (int) $id;
            $pinState = input($id);
            $item['state'] = (int) trim($pinState);

            array_push($response[KEY_DATA_LIST], $item);
        } else {
            $pinNumberArray = array(18, 23);
            foreach ($pinNumberArray as $pinNumber) {
                $item = array();
                $item['pin_number'] = (int) $pinNumber;
                $pinState = input($pinNumber);
                $item['state'] = (int) trim($pinState);

                array_push($response[KEY_DATA_LIST], $item);
            }
        }
        break;

    default:
        $response[KEY_ERROR_CODE] = ERROR_CODE_INVALID_ACTION;
        $response[KEY_ERROR_MESSAGE] = 'No action specified or invalid action.';
        $response[KEY_ERROR_MESSAGE_MORE] = '';
        break;
}

echo json_encode($response);
exit();

function output($pinNumber, $state) {
    $cmd = BASE_COMMAND . " mode $pinNumber out";
    shell_exec($cmd);
    $cmd = BASE_COMMAND . " write $pinNumber $state";
    shell_exec($cmd);
}

function input($pinNumber) {
    $cmd = BASE_COMMAND . " read $pinNumber";
    return shell_exec($cmd);
}

?>