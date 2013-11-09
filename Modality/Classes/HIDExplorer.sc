HIDExplorer {

    classvar <allMsgTypes = #[ \elid, \usage ];

	classvar <resps;
	classvar <results;
	classvar <observeDict;
	classvar <>verbose = true;
	classvar <observedSrcID;

    classvar <exploreFunction;

	*shutUp { verbose = false }

	*init {
        exploreFunction = { |devid, thisdevice, elid, page, usage, value, mappedvalue| this.updateRange( elid, page, usage ) };
	}

	*start { |srcID|
		if (exploreFunction.isNil) { this.init };
		observedSrcID = srcID;
		this.prepareObserve;
        HID.addRecvFunc( exploreFunction );
	}

	*stop {
        HID.removeRecvFunc( exploreFunction );
	}

	*prepareObserve {
		observeDict = ();
		allMsgTypes.do(observeDict.put(_, Dictionary()));
	}

	*openDoc {
		Document("edit and save me", this.compile);
	}

    *openDocFromDevice { |dev|
        Document("edit and save me", this.compileFromDevice( dev ) );
	}

    *detectDuplicateElements{ |elements|
        var elementUsageDict = IdentityDictionary.new;
        var duplicates = IdentityDictionary.new;
        var uniques = IdentityDictionary.new;
        var usagePageKey;

        elements.sortedKeysValuesDo{ |elid,ele|
            usagePageKey = ( ele.usage.asString ++ "_" ++ ele.usagePage ).asSymbol;
            if ( elementUsageDict.at( usagePageKey ).notNil ){
                // this one already appeared, it's a double!!
                duplicates.put( elid, ele );
            }{
                uniques.put( elid, ele );
                elementUsageDict.put( usagePageKey, ele );
            }
        };
        ^[uniques, duplicates];
    }

    *compileFromDevice { |dev|
		var str = "[";
        var elements = dev.elements;
        var uniques, duplicates;

        /// todo: check the device elements whether any duplicate usages occur, if so, then we need to filter by element id
        /// could infer type from the control
        /// could infer name from the control -> suggest a name

        /// FIXME: ignore constant fields!

        #uniques, duplicates = this.detectDuplicateElements( elements.select{ |v| v.ioType == 1 } );
        if ( uniques.size + duplicates.size > 0 ){
            str = str + "\n\n// --------- input elements ----------";
            uniques.sortedKeysValuesDo{ |key,val|
                str = str + "\n'<element name %>': ('usage': %, 'usagePage': %, , 'type': '<type %>', 'ioType': 'in' ),"
                .format(val.usageName, val.usage, val.usagePage, val.pageName );
            };
            duplicates.sortedKeysValuesDo{ |key,val|
                str = str + "\n'<element name %_%>': ('elid': %, 'type': '<type %>', 'ioType': 'in' ),"
                .format(val.usageName, key, key, val.pageName );
            };
        };

        #uniques, duplicates = this.detectDuplicateElements( elements.select{ |v| v.ioType == 2 } );
        if ( uniques.size + duplicates.size > 0 ){
            str = str + "\n\n// --------- output elements ----------";
            uniques.sortedKeysValuesDo{ |key,val|
                str = str + "\n'<element name %>': ('usage': %, 'usagePage': %, , 'type': '<type %>', 'ioType': 'out' ),"
                .format(val.usageName, val.usage, val.usagePage, val.pageName );
            };
            duplicates.sortedKeysValuesDo{ |key,val|
                str = str + "\n'<element name %_%>': ('elid': %, 'type': '<type %>', 'ioType': 'out' ),"
                .format(val.usageName, key, key, val.pageName );
            };
        };

        /*
        #uniques, duplicates = this.detectDuplicateElements( elements.select{ |v| v.ioType == 3 } );
        if ( uniques.size + duplicates.size > 0 ){
            str = str + "\n\n// --------- feature report ----------";
            uniques.sortedKeysValuesDo{ |key,val|
                str = str + "\n'<element name %>': ('usage': %, 'usagePage': %, , 'type': '<type %>', 'ioType': 'feature' ),"
                .format(val.usageName, val.usage, val.usagePage, val.pageName );
            };
            duplicates.sortedKeysValuesDo{ |key,val|
                str = str + "\n'<element name %_%>': ('elid': %, 'type': '<type %>', 'ioType': 'feature' ),"
                .format(val.usageName, key, key, val.pageName );
            };
        };
        */

		str = str + "\n];";

		^str;
    }

	*compileFromObservation { |includeSpecs = false|

		var num, chan;

		var str = "[";

		if (observeDict[\elid].notEmpty) {
			str = str + "\n// ------ element ids -------------";
			observeDict[\elid].sortedKeysValuesDo { |key, val|
				#num, chan = key.split($_).collect(_.asInteger);
				str = str + "\n'<element name %>': ('elid': %, 'type': '<type>'),"
					.format(key, val);
			};
		};

		if (observeDict[\usage].notEmpty) {
			str = str + "\n\n// --------- usage ids ----------";
			observeDict[\usage].sortedKeysValuesDo { |key, val|
				#num, chan = key.split($_).collect(_.asInteger);
				str = str + "\n'<element name %>': ('usage': %, 'usagePage': %, , 'type': '<type>' ),"
				.format(key, val.usage, val.usagePage ); /// could infer type from the control
			};
		};

		str = str + "\n];";

		^str;
	}

	*updateRange { |elid, page, usage|
        var hash, range;
        var msgDict = observeDict[\elid];

        if (verbose) { [elid, page, usage].postcs; } { ".".post; };
        if (0.1.coin) { observeDict.collect(_.size).sum.postln };


        // range = msgDict[hash];
        // range.isNil.if{
        // min max
        // msgDict[hash] = range = [val, val];
    // };

        // msgDict[hash] = [min(range[0], val), max(range[1], val)];
	}
}
