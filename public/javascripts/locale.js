var MessageListItemView = Backbone.View.extend({
	tagName: 'div',
	className: 'message',
	template: _.template($('#message-tmpl').html()),

	render: function() {
		var html = this.template({
			localeName: this.model.get('localeName'),
			value: stripScripts(this.model.get('value'))
		});
		this.$el.html(html);
		return this;
	},

	events: {
		'click a.btn': 'onCopy'
	},

	onCopy: function() {
		Backbone.trigger('message:change', this.model.get('value'));
	}
});
var MessageListView = Backbone.View.extend({
	el: '#panel-messages',

	initialize: function(project, search) {
		this.project = project;
		this.search = search;

		this.listenTo(Backbone, 'item:selected', this.onItemSelected);

		this.listView = this.$('.messages');
	},

	render: function() {
		console.log('MessageListView.render');
		var $list = this.listView.empty();
		var collection = this.collection;

		collection.each(function(model) {
			var item = new MessageListItemView({model: model});
			$list.append(item.render().$el);
		}, this);
	},

	onItemSelected: function(item) {
		if(item === undefined) {
			this.listView.hide();

			if(this.collection !== null) {
				this.stopListening(this.collection, 'sync');
				this.collection = null;
			}

			return;
		}

		this.listView.show();

		this.collection = new MessageList(this.project.id);
		this.listenTo(this.collection, 'sync', this.render);

		var collection = this.collection;
		collection.fetch({data: {keyName: item.get('name')}});
	}

});
var LocaleSelectorListItemView = Backbone.View.extend({
	tagName: 'li',
	template: _.template($('#locale-tmpl').html()),

	initialize: function(arguments) {
		this.listenTo(Backbone, 'item:selected', this.onItemSelected);
	},

	render: function() {
		var keyName = '';
		if(this.message !== undefined) {
			keyName = this.message.get('name');
		}

		var html = this.template({
			id: this.model.id,
			url: jsRoutes.controllers.Locales.localeBy(
					this.model.get('projectOwnerUsername'),
					this.model.get('projectName'),
					this.model.get('name')
				).url,
			localeName: this.model.get('name'),
			keyName: keyName
		});
		this.$el.html(html);
		return this;
	},

	onItemSelected: function(item) {
		this.message = item;
		this.render();
	}
});
var LocaleSelectorListView = Backbone.View.extend({
	el: '#dropdown-locales',

	initialize: function(project) {
		this.collection = project.locales;

		this.listenTo(this.collection, 'sync', this.render);

		var collection = this.collection;
		this.collection.fetch({data:{order:'name'}});
	},

	render: function() {
		var $list = this.$el.empty();
		var collection = this.collection;
		collection.each(function(model) {
			var item = new LocaleSelectorListItemView({model: model});
			$list.append(item.render().$el);
		}, this);
	}
});
var EditorSwitchView = Backbone.View.extend({
	el: '#switch-editor',

	initialize: function(localeName) {
		this.localeName = localeName;

		this.listenTo(Backbone, 'item:selected', this.onItemSelected);
	},

	onItemSelected: function(item) {
		if(item === undefined) {
			this.$el.attr('href', '#');
			this.$el.addClass('disabled');

			return;
		}

		this.$el.attr(
			'href',
			jsRoutes.controllers.Keys.keyBy(
					item.get('projectOwnerUsername'),
					item.get('projectName'),
					item.get('pathName')
			).url + '#locale/' + this.localeName
		);
		this.$el.removeClass('disabled');
	}
});

var LocaleEditor = Editor.extend({
	initialize: function() {
		Editor.prototype.initialize.apply(this, arguments);

		this.itemType = 'key';
		this.itemList = new ItemListView(
			this.project,
			this.project.keys,
			this.search,
			'keys',
			this.itemType,
			this.localeName
		);
		this.messageList = new MessageListView(this.project, this.search);
		this.localeSelector = new LocaleSelectorListView(this.project);
		this.editorSwitch = new EditorSwitchView(this.locale.name);
	},

	selectedItem: function(itemName) {
		if(itemName !== null) {
			return this.project.keys.find(function(item) {
				return item.get('name') === itemName;
			});
		}
		return undefined;
	},

	onItemSelected: function(model) {
		Editor.prototype.onItemSelected.apply(this, arguments);

		this.itemList.$el.find('.active').removeClass('active');
		if(model !== undefined) {
			this.keyId = model.id;
			this.keyName = model.get('name');
			this.itemList.$el.find('#' + model.id).addClass('active');
		} else {
			this.keyId = null;
			this.keyName = null;
		}
	}
});

App.Modules.LocaleHashModule = function(sb) {
	var params = $.deparam.fragment();
	var location = window.location;
	var doc = sb.dom.wrap(document);
	var keys = sb.dom.find('.collection.keys');

	function _initFromHash() {
		if('key' in params) {
			var $a = sb.dom.find('a.key[keyName="'+params.key+'"]');
			keys.animate(
				{ scrollTop: _scrollTopOffset(keys, $a) },
				'500'
			);
			$a[0].click();
		} else {
			var $key = sb.dom.find('.keys .collection-item.key:first-child');
			params.key = $key.attr('keyName');
			$key[0].click();
		}
	}

	function _scrollTopOffset(container, item) {
		return - container.position().top - container.css('margin-top').replace('px', '') + item.parent().offset() ? item.parent().offset().top : 0
	}

	return {
		create : function() {
			doc.ready(_initFromHash);
		},
		destroy : function() {
		}
	};
};
