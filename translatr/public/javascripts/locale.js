/*
 * Input:
 * 
 * * projectId: Project.id
 * * localeId: Locale.id
 * * locales: List of Locale
 */

function handleMessage(message) {
    $('#field-id').val(message.id);
    $('#field-key').val(message.key.name).attr('keyId', message.key.id);
    $('#field-value').val(message.value);
    $('#preview').html(message.value);
}
function handleMessageList(keyName, messages) {
    var $messages = $('#panel-messages');
    $messages.find('.message:not(.template)').remove();
    var $template = $('.message.template');
    messages.forEach(function(entry) {
    	console.log('Message: ', entry);
    	var $msg = $template.clone().removeClass('template');
    	$msg.attr('href', window.location.hash).click(handleCopyMessageValue);
    	$msg.find('.localeName').text(locales[entry.localeId]);
    	$msg.find('.value').html(entry.value);
    	$messages.append($msg);
    });
}
function handleCopyMessageValue() {
	var value = $(this).find('.value').text();
	$('#field-value').val(value);
	$('#preview').html(value);
}
function handleSaveMessage(message) {
    if(message.locale.id === localeId) {
    	$('#' + message.key.id).removeClass('no-message');
    	$('#' + message.key.id + ' .value').html(message.value);
    	updateForm(message.key.id, $('#field-key').val());
    }
}
function updateForm(keyId, keyName) {
	$("#no-selection").hide();
	$("#panel-message").show();
	$("#panel-messages").show();
    $('#field-id').val('');
	$('#field-key').val(keyName).attr('keyId', keyId);
    $('#field-value').val('');
    $('#preview').html('');
	$.ajax(
		jsRoutes.controllers.Api.getMessage(localeId, keyName)
	).done(handleMessage);
	$.ajax($.extend(
		jsRoutes.controllers.Api.findMessages(projectId),
		{data: {keyName: keyName}}
	)).done(function(data) {
	    handleMessageList(keyName, data);
	});
}
function search(value) {
	var filterUntranslated = $('#field-untranslated').is(':checked');
    if(value !== '') {
		$('a.key').hide();
		if(filterUntranslated) {
			console.log('Untranslated checked');
			$('a.key.no-message[name*="' + value.toLowerCase() +'"]').show();
		} else {
			console.log('Untranslated unchecked');
			$('a.key[name*="' + value.toLowerCase() +'"]').show();
		}
    } else {
		if(filterUntranslated) {
			console.log('Untranslated checked');
			$('a.key').hide();
	    	$('a.key.no-message').show();
		} else {
			console.log('Untranslated unchecked');
	    	$('a.key').show();
		}
    }
}
$(document).ready(function() {
	$("#panel-message").hide();
	$("#panel-messages").hide();
	$('#field-search').on('change keyup paste', function() {
		search($('#field-search').val());
	});
	$('#field-untranslated').change(function() {
		search($('#field-search').val());
	});
	$('#form-search').submit(function(e) {
		console.log('Hide...');
        e.preventDefault();
        search($('#field-search').val());
	});
	$("#form-message").submit(function(e){
        e.preventDefault();
		$.ajax(
			$.extend(
				jsRoutes.controllers.Api.putMessage(),
				{
					contentType: 'application/json',
					dataType: 'json',
					data: JSON.stringify({
						"id": $('#field-id').val() !== '' ? $('#field-id').val() : null,
						"locale": {
						  "id": localeId
						},
						"key": {
							"id": $('#field-key').attr('keyId')
						},
						"value": $('#field-value').val()
					})
				}
			)
		).done(function(data) {
			handleSaveMessage(data);
		});
    });
	$('a.key').click(function() {
		$('a.key').removeClass('active');
		$(this).addClass('active')
		updateForm($(this).attr('id'), $(this).attr('name'));
	})
	$('a.key .btn-remove').click(function(e) {
		e.stopPropagation();
		window.location.href=jsRoutes.controllers.Application.keyRemove($(this).parent().attr('id'), localeId).url;
	});
	$('#field-value').on('change keyup paste', function() {
	    $('#preview').html($('#field-value').val());
	});
	$('.btn-cancel').click(function() {
		$('a.key').removeClass('active');
		$("#panel-message").hide();
		$("#panel-messages").hide();
		$("#no-selection").show();
		window.location.hash = '#';
	});
	$('.button-save').click(function() {
		$('#form-key').attr('action', $('#form-key').attr('action') + '#key=' + $('#field-key-name').val());
		$('#form-key').submit();
	});

	var hash = window.location.hash;
	if(hash !== '') {
		console.log('Hash: ', hash);
		var keyName = hash.replace('#key=', '');
		var $a = $('a.key[name="'+keyName+'"]');
		$a[0].scrollIntoView();
		$a.click();
	} else {
		var $key = $('a.key:first-child');
		window.location.hash = $key.attr('href').replace('.*#', '#');
		$key.click();
	}
});
