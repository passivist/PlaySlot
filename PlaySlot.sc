/*
* PlaySlot is a class for managing and drawing Ableton SessionView Style GUIs
*
* TODO:
*		make everything more general
*		add prepareToRecord and  recording methods
*		filled and empty should be seperate entities from the playstates
*
*
*		Grain Synth Density
*/

PlaySlot {
	var <state, <isFilled, <>isClicked, buttonView, view, <mouseDownAction, <row, <column, intermediateBuffer, <buffer, <>synthSlot, <name, nameText, time;

	*new { arg parent, row=0, column=0;
		var p = parent.asView;
		^super.new.init(p, row, column);
	}

	init { arg parent, rowOutside, columnOutside;
		var x, y;

		row = rowOutside;
		column = columnOutside;

		x = row * 110;
		y = column * 30;

		name = "";
		nameText = StaticText.new(view, Rect(30, 5, 90, 20)).string_(name);

		view = UserView.new(parent, Rect(x, y, 110, 30));
		view.mouseDownAction_{ |view|
			if(isClicked){ isClicked = false }{ isClicked = true };
			view.background()
		},

		buttonView = UserView.new(view, Rect(0, 0, 30, 30)).animate_(true).frameRate_(2);

		this.isFilled_(false);
		this.changeState("stopped");

	}

	mouseDownAction_{ arg aFunction;
		mouseDownAction = aFunction;
		buttonView.mouseDownAction = aFunction;
	}

	playSynth {
		if(buffer.notNil){
			synthSlot = Synth(\player, [\buf, buffer, \loop, 1]);
			postf("playSynth: %\n", synthSlot );
		}
	}

	recordSynth {
		synthSlot = Synth(\recorder, [\buf, intermediateBuffer]);
	}

	stopSynth {
		if(synthSlot.notNil){
			this.synthSlot.free;
			this.synthSlot = nil;
		}
	}

	buffer_ { |buf|
		buffer = buf;
		this.isFilled_(true);
	}

	name_{ |string|
		name = string.asString;
		nameText = StaticText.new(view, Rect(30, 5, 90, 20)).string_(name);
		view.refresh;
	}

	changeState {|state|
		switch (state,
			"stopped",				{ this.stateStopped				},
			"stopping",				{ this.stateStopping				},
			"prepareToPlay",		{ this.statePrepareToPlay		},
			"playing",				{ this.statePlaying				},
			"prepareToRecord",	{ this.statePrepareToRecord	},
			"recording",			{ this.stateRecording			},
			"stopRecording",		{ this.stateStopRecording		}
		);
		view.parent.refresh;
	}

	isFilled_{ |bool|
		if(bool){
			this.stateFilled;
			isFilled = true
		}{
			this.stateEmpty;
			isFilled = false;
		}
	}

	stateEmpty {
		view.drawFunc_{

		};

		buttonView.drawFunc_{ |view|
			Pen.color_(Color.black);
			Pen.width_(2);
			Pen.addRect(Rect(5, 5, 20, 20));
			Pen.stroke;
		};
	}

	stateFilled {
		view.background_(Color.gray(0.5));
		view.drawFunc_{
			Pen.color_(Color.black);
			Pen.width_(1);
			Pen.smoothing_(false);
			Pen.addRect(Rect(1, 1, 108, 28));
			Pen.stroke;
		};

		buttonView.drawFunc_{ |view|
			Pen.color_(Color.black);
			Pen.width_(2);
			Pen.addRect(Rect(5, 5, 20, 20));
			Pen.stroke;
		};

		nameText.string_(name);
	}

	stateStopped {
		buttonView.drawFunc_{ |view|
			Pen.color_(Color.black);
			Pen.width_(2);
			Pen.addRect(Rect(5, 5, 20, 20));
			Pen.stroke;
		};

		this.stopSynth;

		state = "stopped";
	}

	stateStopping {
		buttonView.drawFunc_{ |view|
			if(view.frame % 2 == 0){
				Pen.width_(2);
				Pen.color_(Color.yellow);
				Pen.addRect(Rect(5, 5, 20, 20));
				Pen.stroke;
			} {
				Pen.width_(2);
				Pen.color_(Color.black);
				Pen.addRect(Rect(5, 5, 20, 20));
				Pen.stroke;
				Pen.stroke;
			}
		};

		state = "stopping";
	}

	statePrepareToPlay {
		buttonView.drawFunc_{ |view|

			if(view.frame % 2 == 0){
				Pen.width_(2);
				Pen.color_(Color.yellow);
				Pen.moveTo(5@5);
				Pen.lineTo(5@25);
				Pen.lineTo(25@15);
				Pen.lineTo(5@5);
				Pen.stroke;
			} {
				Pen.width_(2);
				Pen.color_(Color.black);
				Pen.moveTo(5@5);
				Pen.lineTo(5@25);
				Pen.lineTo(25@15);
				Pen.lineTo(5@5);
				Pen.stroke;
			}
		};

		state = "prepareToPlay";
	}

	statePlaying {
		"playing".postln;
		buttonView.drawFunc_{
			Pen.width_(2);
			Pen.moveTo(5@5);
			Pen.lineTo(5@25);
			Pen.lineTo(25@15);
			Pen.lineTo(5@5);

			Pen.strokeColor_(Color.black);
			Pen.fillColor_(Color.green);
			Pen.draw(4);
		};

		if(synthSlot.notNil){
			this.stopSynth;
		};

		this.playSynth;

		state = "playing";
		//state.postln;
	}

	statePrepareToRecord {
		intermediateBuffer = Buffer.alloc(Server.default, Server.default.sampleRate * 120, 2);

		buttonView.drawFunc_{ |view|
			Pen.strokeColor_(Color.black);
			Pen.fillColor_(Color.red);
			Pen.width_(2);
			Pen.smoothing_(false);
			Pen.circle(Rect(5, 5, 20, 20));
			Pen.draw(3);
		};
		this.isFilled_(true);
		state = "prepareToRecord";

		"prepareToRecord".postln;
	}

	stateRecording {
		buttonView.drawFunc_{
			Pen.strokeColor_(Color.black);
			Pen.fillColor_(Color.red);
			Pen.width_(2);
			Pen.smoothing_(false);
			Pen.circle(Rect(5, 5, 20, 20));
			Pen.draw(3);
		};

		if(synthSlot.notNil){
			this.stopSynth;
		};
		if(buffer.notNil){
			buffer = nil;
		};
		this.recordSynth;
		time = AppClock.seconds;
		state = "recording";
		"recording".postln;
	}

	stateStopRecording {
		var endTime = AppClock.seconds;
		var length = endTime - time;
		length.postln;
		// will this cause timing issues?
		buffer = Buffer.alloc(Server.default, length * Server.default.sampleRate, 2);
		intermediateBuffer.copyData(buffer, 0, 0, length * Server.default.sampleRate);
		this.stopSynth;
		intermediateBuffer = nil;
		state = "stopRecording";

		"stopRecording".postln;
	}
}

RecordButton {
	var <>state, buttonView, view, <mouseDownAction, <row;

	*new { arg parent, col=0;
		var p = parent.asView;
		^super.new.init(p, col);
	}

	init { arg parent, rowOutside;
		var x, y;

		row = rowOutside;

		x = (row * 110) + 55;
		y = 255;

		view = UserView.new(parent, Rect(x, y, 55, 30));
		buttonView = UserView.new(view, Rect(0, 0, 30, 30));

		this.changeState(false);
		state = false;

		view.parent.refresh;
	}

	mouseDownAction_ { |aFunction|
		mouseDownAction = aFunction;
		buttonView.mouseDownAction = aFunction;
	}

	changeState { |outerState|
		if(outerState){
			buttonView.drawFunc_{
				Pen.strokeColor_(Color.black);
				Pen.fillColor_(Color.red);
				Pen.width_(2);
				Pen.smoothing_(false);
				Pen.circle(Rect(1, 1, 18, 18));
				Pen.draw(3);
			};

			state = true;
		}{
			buttonView.drawFunc_{
				Pen.color_(Color.black);
				Pen.width_(2);
				Pen.smoothing_(false);
				Pen.circle(Rect(1, 1, 18, 18));
				Pen.stroke;
			};

			state = false;
		};

		view.parent.refresh;
	}
}

StopButton {
	var buttonView, view, <mouseDownAction, <row;

	*new { arg parent, row=0;
		var p = parent.asView;
		^super.new.init(p, row);
	}

	init { arg parent, rowOutside;
		var x, y;

		row = rowOutside;

		x = row * 110;
		y = 255;

		view = UserView.new(parent, Rect(x, y, 55, 30));
		buttonView = UserView.new(view, Rect(27, 0, 30, 30));

		buttonView.drawFunc_{
			Pen.color_(Color.black);
			Pen.width_(2);
			Pen.smoothing_(false);
			Pen.addRect(Rect(1, 1, 18, 18));
			Pen.stroke;
		};

		view.parent.refresh;
	}

	mouseDownAction_ { |aFunction|
		mouseDownAction = aFunction;
		buttonView.mouseDownAction = aFunction;
	}

}