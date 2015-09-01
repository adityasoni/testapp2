<!DOCTYPE html>
<html>
<head>
    <title>Credit Card Management</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
    <link rel="stylesheet" href="http://s3-us-west-2.amazonaws.com/webapp-fn/magnific-popup.css">
    <link rel="stylesheet" href="http://s3-us-west-2.amazonaws.com/webapp-fn/table.css">
    <link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
    <script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
    <script src="http://s3-us-west-2.amazonaws.com/webapp-fn/jquery.magnific-popup.js"></script>
    <script type="application/javascript">

        $(document).ready(function() {
            $('.open-popup-link').magnificPopup({type:'inline', midClick: true });
            $.magnificPopup.instance.close = function () {
                location.reload();
                // "proto" variable holds MagnificPopup class prototype
                // The above change that we did to instance is not applied to the prototype,
                // which allows us to call parent method:
                $.magnificPopup.proto.close.call(this);
            };
        });


        function loadBalance() {
            tHeadMain = $('<thead>');
            tHead = $('<tr>');

            tHead.append($('<th>').html("Card Company"));
            tHead.append($('<th>').html("Initial Balance"));
            tHead.append($('<th>').html("Balance Left"));
            tHead.append($('<th>').html("Due Date"));
            tHead.append($('<th>').html("Time Left"));
            tHead.append($('<th>').html("Progress"));

            tHeadMain.append(tHead);
            $('#balance_table').append(tHeadMain);

            $.getJSON('/finance/balances', null, function (data) {
                $.each(data, function (i, item) {

                    tRow = $('<tr>');

                    tRow.append($('<td>').html(item.bank_name));
                    tRow.append($('<td>').html(item.initial_balance));
                    tRow.append($('<td>').html(item.balance_left));
                    tRow.append($('<td>').html(item.due_date));
                    tRow.append($('<td>').html(item.time_left));
                    tRow.append($('<td>').html(createProgressBar(item.balance_left, item.initial_balance)));
                    $('#balance_table').append(tRow);
                    $('#myselect').append($('<option>').text(item.bank_name).attr('value', item.id));

                });
            });
        }

        function createProgressBar(balanceLeft, initialBalance) {
            var balanceLeftNumber = Number(balanceLeft.replace(/[^0-9\.]+/g,""));
            var initialBalanceNumber = Number(initialBalance.replace(/[^0-9\.]+/g,""));

            var percentLeft = ((balanceLeftNumber/initialBalanceNumber)*100).toFixed(0);
            if(percentLeft < 1) {
                percentLeft = 0;
                return '<div class="progress"> <div class="progress-bar progress-bar-success progress-bar-striped active" role="progressbar" aria-valuenow="' + percentLeft + '"aria-valuemin="0" aria-valuemax="100" style="width:'+percentLeft+'%"><div style="color:#000"> '+percentLeft+'% Left (success)</div> </div> </div>';
            } else if(percentLeft < 40) {
                return '<div class="progress"> <div class="progress-bar progress-bar-success progress-bar-striped active" role="progressbar" aria-valuenow="' + percentLeft + '"aria-valuemin="0" aria-valuemax="100" style="width:'+percentLeft+'%"><div style="color:#000"> '+percentLeft+'% Left (success)</div> </div> </div>';
            } else if(percentLeft < 60){
                return '<div class="progress"> <div class="progress-bar progress-bar-info progress-bar-striped active" role="progressbar" aria-valuenow="' + percentLeft + '"aria-valuemin="0" aria-valuemax="100" style="width:'+percentLeft+'%"><div style="color:#000"> '+percentLeft+'% Left (almost there)</div> </div> </div>';
            } else if(percentLeft < 80){
                return '<div class="progress"> <div class="progress-bar progress-bar-warning progress-bar-striped active" role="progressbar" aria-valuenow="' + percentLeft + '"aria-valuemin="0" aria-valuemax="100" style="width:'+percentLeft+'%"><div style="color:#000"> '+percentLeft+'% Left (reaching) </div></div> </div>';
            } else {
                return '<div class="progress"> <div class="progress-bar progress-bar-danger progress-bar-striped active" role="progressbar" aria-valuenow="' + percentLeft + '"aria-valuemin="0" aria-valuemax="100" style="width:'+percentLeft+'%"><div style="color:#000"> '+percentLeft+'% Left (way to go) </div></div> </div>';
            }
        }

        function getBankDetail(sel) {
            if(sel.value == 'blank') {
                $('#card-detail-form').html('');
            } else if(sel.value == 'addNewCC_126') {
                $('#card-detail-form').html('');

                tTable = $('<table>');

                tRowHD = $('<tr>');
                tRowHD.append($('<th colspan="2">').html('Add New Credit Card'));
                tTable.append(tRowHD);

                tRowBN = $('<tr>');
                tRowBN.append($('<td>').html('Bank Name'));
                tRowBN.append($('<td>').html('<input type="text" id="bank_name_to_add" />'));
                tTable.append(tRowBN);

                tRowIB = $('<tr>');
                tRowIB.append($('<td>').html('Initial Balance'));
                tRowIB.append($('<td>').html('<input type="text" id="initial_balance_to_add" />'));
                tTable.append(tRowIB);

                tRowBL = $('<tr>');
                tRowBL.append($('<td>').html('Balance Left'));
                tRowBL.append($('<td>').html('<input type="text" id="balance_left_to_add" />'));
                tTable.append(tRowBL);

                tRowDD = $('<tr>');
                tRowDD.append($('<td>').html('Due Date'));
                tRowDD.append($('<td>').html('<input type="text" id="due_date_to_add" />'));
                tTable.append(tRowDD);

                tRowUser = $('<tr>');
                tRowUser.append($('<td>').html('User Name'));
                tRowUser.append($('<td>').html('<input type="text" id="user_name_for_post" />'));
                tTable.append(tRowUser);

                tRowPass = $('<tr>');
                tRowPass.append($('<td>').html('Password'));
                tRowPass.append($('<td>').html('<input type="password" id="password_for_post" />'));
                tTable.append(tRowPass);

                tRowAction =  $('<tr>');
                tRowAction.append($('<td colspan="2">').html('<input type="button" id="add_credit_card" value="ADD" onclick="addCardDetail(this)"  />'));
                tTable.append(tRowAction);

                $('#card-detail-form').append(tTable);
                $('#card-detail-form').append('<div id="status-message" />');
            } else {
                $('#card-detail-form').html('');
                var getByIdURL = '/finance/balances/id/' + sel.value + '?prettyFormat=no';
                $.getJSON(getByIdURL, null, function (data) {
                    $('#card-detail-form').append('<input type="hidden" id="bank_id_to_update" value='+data.id+' />');
                    $('#card-detail-form').append('<input type="hidden" id="bank_name_to_update" value='+data.bank_name+' />');
                    tTable = $('<table>');
                    tRowBN = $('<tr>');
                    tRowBN.append($('<th colspan="2">').html('Bank Name: ' + data.bank_name));
//                    tRowBN.append($('<th>').html('<input type="text" id="initial_balance_to_update" value='+data.bank_name+' disabled />'));
                    tTable.append(tRowBN);

                    tRowIB = $('<tr>');
                    tRowIB.append($('<td>').html('Initial Balance'));
                    tRowIB.append($('<td>').html('<input type="text" id="initial_balance_to_update" value='+data.initial_balance+' />'));
                    tTable.append(tRowIB);

                    tRowBL = $('<tr>');
                    tRowBL.append($('<td>').html('Balance Left'));
                    tRowBL.append($('<td>').html('<input type="text" id="balance_left_to_update" value='+data.balance_left+' />'));
                    tTable.append(tRowBL);

                    tRowDD = $('<tr>');
                    tRowDD.append($('<td>').html('Due Date'));
                    tRowDD.append($('<td>').html('<input type="text" id="due_date_to_update" value='+data.due_date+' />'));
                    tTable.append(tRowDD);

                    tRowUser = $('<tr>');
                    tRowUser.append($('<td>').html('User Name'));
                    tRowUser.append($('<td>').html('<input type="text" id="user_name_for_update" />'));
                    tTable.append(tRowUser);

                    tRowPass = $('<tr>');
                    tRowPass.append($('<td>').html('Password'));
                    tRowPass.append($('<td>').html('<input type="password" id="password_for_update" />'));
                    tTable.append(tRowPass);

                    tRowAction =  $('<tr>');
                    tRowAction.append($('<td>').html('<input type="button" id="update_balnce" value="UPDATE" onclick="updateCardDetail(this)"  />'));
                    tRowAction.append($('<td>').html('<input type="button" id="delete_balnce" value="DELETE" onclick="deleteCardDetail(this)"  />'));
                    tTable.append(tRowAction);

//                    tRowStatusMessage = $('<tr>');
//                    tRowStatusMessage.append($('<td colspan="2" height="10px">').html('<div id="status-message" />'));
//                    tTable.append(tRowStatusMessage);
//
//                    tRowStatusMessage1 = $('<tr>');
//                    tRowStatusMessage1.append($('<td colspan="2" height="50%">').html('<div id="status-message1" />'));
//                    tTable.append(tRowStatusMessage1);

                    $('#card-detail-form').append(tTable);

                    $('#card-detail-form').append('<div id="status-message" />');

//                    $('#card-detail-form').append('<br/> Bank Name: <input type="text" id="initial_balance_to_update" value='+data.bank_name+' disabled /><br/>');
//                    $('#card-detail-form').append('Initial Balance: <input type="text" id="initial_balance_to_update" value='+data.initial_balance+' /><br/>');
//                    $('#card-detail-form').append('Balance Left: <input type="text" id="balance_left_to_update" value='+data.balance_left+' /><br/>');
//                    $('#card-detail-form').append('Due Date: <input type="text" id="due_date_to_update" value='+data.due_date+' /><br/>');
//                    $('#card-detail-form').append('<input type="button" id="update_balnce" value="UPDATE" onclick="updateCardDetail(this)"  />');
//                    $('#card-detail-form').append('<input type="button" id="delete_balnce" value="DELETE" onclick="deleteCardDetail(this)"  />');
                });
            }
        }

        function updateCardDetail(formData) {
            var updatedBankName = $( "input#bank_name_to_update" ).val();
            var updatedInitialBalance = $( "input#initial_balance_to_update" ).val();
            var updatedBalanceLeft = $( "input#balance_left_to_update" ).val();
            var updatedDueDate = $( "input#due_date_to_update" ).val();
            var email = $( "input#user_name_for_update" ).val();
            var password = $( "input#password_for_update" ).val();

            var dataToUpdate = '{"bank_name": "'+updatedBankName+'", "initial_balance": "'+updatedInitialBalance+'", "balance_left": "'+updatedBalanceLeft+'", "due_date": "'+updatedDueDate+'"}';

            $.ajax({
                url: '/finance/balances?email='+email+'&password='+password,
                data: dataToUpdate,
                accept: 'application/json',
                contentType: 'application/json',
                type: 'PUT',
                success: function(response) {
                    $('#status-message').html('');
                    $('#status-message').css('background-color', '').css('background-color', '#9fff79');
                    $('#status-message').append('Successfully updated '+ updatedBankName);
                    $('#status-message').fadeIn('slow').delay(3000).fadeOut('slow');
                },
                error: function(response) {
                    $('#status-message').html('');
                    $('#status-message').css('background-color', '').css('background-color', '#ffae83');
                    $('#status-message').append('Failed to Update Record.<br/>Staus Code: '+ response.status +'<br/> Error Message:' +
                            jQuery.parseJSON(response.responseText).ErrorMessage);
                    $('#status-message').fadeIn('slow').delay(3000).fadeOut('slow');
                }
            });
        }

        function addCardDetail(formData) {
            var bankNameToAdd = $( "input#bank_name_to_add" ).val();
            var initialBalanceToAdd = $( "input#initial_balance_to_add" ).val();
            var balanceLeftToAdd = $( "input#balance_left_to_add" ).val();
            var dueDateToAdd = $( "input#due_date_to_add" ).val();
            var email = $( "input#user_name_for_post" ).val();
            var password = $( "input#password_for_post" ).val();

            var dataToAdd = '{"bank_name": "'+bankNameToAdd+'", "initial_balance": "'+initialBalanceToAdd+'", "balance_left": "'+balanceLeftToAdd+'", "due_date": "'+dueDateToAdd+'"}';

            $.ajax({
                url: '/finance/balances?email='+email+'&password='+password,
                data: dataToAdd,
                accept: 'application/json',
                contentType: 'application/json',
                type: 'POST',
                success: function(response) {
                    $('#status-message').html('');
                    $('#status-message').css('background-color', '').css('background-color', '#9fff79');
                    $('#status-message').append('Successfully added '+ bankNameToAdd);
                    $('#status-message').fadeIn('slow').delay(3000).fadeOut('slow');

                    alert('Successfully added '+ bankNameToAdd);

                    $('#myselect').append($('<option>').text(response.bank_name).attr('value', response.id));
                    $("#myselect").val(response.id).change();
                },
                error: function(response) {
                    $('#status-message').html('');
                    $('#status-message').css('background-color', '').css('background-color', '#ffae83');
                    $('#status-message').append('Failed to Add Record.<br/>Staus Code: '+ response.status +'<br/> Error Message:' +
                            jQuery.parseJSON(response.responseText).ErrorMessage);
                    $('#status-message').fadeIn('slow').delay(3000).fadeOut('slow');
                }
            });
        }

        function deleteCardDetail(formData) {
            var id = $( "input#bank_id_to_update" ).val();
            var updatedBankName = $( "input#bank_name_to_update" ).val();
            var email = $( "input#user_name_for_update" ).val();
            var password = $( "input#password_for_update" ).val();

            $.ajax({
                url: '/finance/balances/id/'+id+'?email='+email+'&password='+password,
                accept: 'application/json',
                contentType: 'application/json',
                type: 'DELETE',
                success: function(response) {
                    $('#status-message').html('');
                    $('#status-message').css('background-color', '').css('background-color', '#9fff79');
                    $('#status-message').append('Successfully deleted '+ updatedBankName);
                    $('#status-message').fadeIn('slow').delay(3000).fadeOut('slow');

                    alert('Successfully deleted '+ updatedBankName)
                    var optionId = $( "input#bank_id_to_update" ).val();
                    $('#myselect option[value="'+optionId+'"]').remove();
                    $("#myselect").val('blank').change();
                },
                error: function(response) {
                    $('#status-message').html('');
                    $('#status-message').css('background-color', '').css('background-color', '#ffae83');
                    $('#status-message').append('Failed to Delete Record.<br/>Staus Code: '+ response.status +'<br/> Error Message:' +
                            jQuery.parseJSON(response.responseText).ErrorMessage);
                    $('#status-message').fadeIn('slow').delay(2000).fadeOut('slow');
                }
            });
        }


    </script>
    <style>
        .white-popup {
            position: relative;
            background: #FFF;
            padding: 20px;
            width: auto;
            max-width: 500px;
            margin: 20px auto;
        }
    </style>
</head>

<body onload="loadBalance()">

<table id="balance_table"/>
<table id="popup-table">
    <tr>
        <td>
            <a href="#test-popup" class="open-popup-link">Manage Cards</a>
        </td>
    </tr>
</table>

<div id="test-popup" class="white-popup mfp-hide">
    Select from dropdown to update detail<br/>
    <select id="myselect" name="myselect" onchange="getBankDetail(this)">
        <option selected="selected" value="blank">-- select --</option>
        <option value="addNewCC_126">** Add New Card</option>
    </select>
    <form id="card-detail-form"/>
</div>



</body>
</html>