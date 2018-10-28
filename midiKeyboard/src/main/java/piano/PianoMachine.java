package piano;

import javax.sound.midi.MidiUnavailableException;
import midi.Instrument;
import midi.Midi;
import music.NoteEvent;
import music.Pitch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.max;
import static java.util.Collections.min;

public class PianoMachine {
	
	private Midi midi;
    private ArrayList<Pitch> soundingPitch = new ArrayList<Pitch>();
	private Instrument ins = Instrument.PIANO;
	private int insNum = 0;
	private int shift = 0;
    private boolean recording = false;
    private ArrayList<NoteEvent> noteEvents = new ArrayList<NoteEvent>();
	/**
	 * constructor for PianoMachine.
	 * 
	 * initialize midi device and any other state that we're storing.
	 */
    public PianoMachine() {
    	try {
            midi = Midi.getInstance();
        } catch (MidiUnavailableException e1) {
            System.err.println("Could not initialize midi device");
            e1.printStackTrace();
            return;
        }
    }
    
    //TODO write method spec
    public void beginNote(Pitch rawPitch) {
    	//midi.beginNote(new Pitch(0).toMidiFrequency());
    	//TODO implement for question 1
        Pitch playingPitch = rawPitch.transpose(Pitch.OCTAVE*shift);
        //midi.beginNote(playingPitch.toMidiFrequency(),this.ins);
        if (soundingPitch.indexOf(playingPitch) < 0){
            midi.beginNote(playingPitch.toMidiFrequency(),this.ins);
            soundingPitch.add(playingPitch);
        }
        if(recording){
            NoteEvent anotherNote = new NoteEvent(playingPitch, currentTimeMillis(), this.ins, NoteEvent.Kind.start);
            noteEvents.add(anotherNote);
        }
    }
    
    //TODO write method spec
    public void endNote(Pitch rawPitch) {
    	//midi.endNote(new Pitch(0).toMidiFrequency());
    	//TODO implement for question 1
        Pitch playingPitch = rawPitch.transpose(Pitch.OCTAVE*shift);
        if (soundingPitch.indexOf(playingPitch)>=0){
            midi.endNote(playingPitch.toMidiFrequency(),this.ins);
            soundingPitch.remove(playingPitch);
        }
        if(recording){
            NoteEvent endingNote = new NoteEvent(playingPitch, currentTimeMillis(), this.ins, NoteEvent.Kind.stop);
            noteEvents.add(endingNote);
        }
    }
    
    //TODO write method spec
    public void changeInstrument() {
       	//TODO: implement for question 2
        if (this.insNum == Instrument.values().length-1){
            this.insNum = 0;
            this.ins = Instrument.PIANO;
        }else {
            this.insNum += 1;
            this.ins = Instrument.values()[insNum];
        }
    }
    
    //TODO write method spec
    public void shiftUp() {
    	//TODO: implement for question 3
        if(this.shift<2){
            this.shift += 1;
        }
    }
    
    //TODO write method spec
    public void shiftDown() {
    	//TODO: implement for question 3
        if(this.shift>-2){
            this.shift -= 1;
        }
    }
    
    //TODO write method spec
    public boolean toggleRecording() {
        if(this.recording == true){//end recording
            this.recording = false;
            return false;
        }else{//start recording
            noteEvents = new ArrayList<NoteEvent>();
            this.recording = true;
            return true;
        }
    }
    
    //TODO write method spec
    public void playback() {
        //TODO: implement for question 4
        ArrayList<Long> timeline = new ArrayList<Long>();
        for (NoteEvent notes : noteEvents) {
            timeline.add(notes.getTime());
        }
        long startTime = min(timeline);
        long stopTime = max(timeline);
        long timeLength = stopTime - startTime;
        HashMap<Long, ArrayList<Sound>> beginTime = new HashMap<>();
        HashMap<Long, ArrayList<Sound>> endTime = new HashMap<>();
        for (NoteEvent oneNote : noteEvents) {
            if (oneNote.getKind().equals(NoteEvent.Kind.start)) {
                if (beginTime.containsKey(oneNote.getTime() - startTime)) {
                    beginTime.get(oneNote.getTime() - startTime).add(new Sound(oneNote.getPitch(), oneNote.getInstr()));
                } else {
                    beginTime.put(oneNote.getTime() - startTime, new ArrayList<Sound>(Arrays.asList(new Sound(oneNote.getPitch(), oneNote.getInstr()))));
                }
            } else {
                if (oneNote.getKind().equals(NoteEvent.Kind.stop)) {
                    if (endTime.containsKey(oneNote.getTime() - startTime)) {
                        endTime.get(oneNote.getTime() - startTime).add(new Sound(oneNote.getPitch(), oneNote.getInstr()));
                    } else {
                        endTime.put(oneNote.getTime() - startTime, new ArrayList<Sound>(Arrays.asList(new Sound(oneNote.getPitch(), oneNote.getInstr()))));
                    }
                }
            }
        }
//        System.out.println(beginTime);
//        System.out.println(endTime);
        Long now = currentTimeMillis();
        while (currentTimeMillis() - now < timeLength+100) {
            long timePassed = currentTimeMillis() - now;
            //System.out.println(timePassed);
            if (beginTime.containsKey(timePassed)) {
                for (Sound oneSound : beginTime.get(timePassed)) {
                    beginGoodPitch(oneSound.pitch, oneSound.instru);
                    //System.out.println("starting pitch "+ oneSound.pitch.toString());
                }
            }
            if (endTime.containsKey(timePassed)) {
                for (Sound oneSound : endTime.get(timePassed)) {
                    endGoodPitch(oneSound.pitch, oneSound.instru);
                    //System.out.println("ending pitch "+ oneSound.pitch.toString());
                }
            }
        }
    }


    void beginGoodPitch(Pitch playingPitch, Instrument instru){
        if (soundingPitch.indexOf(playingPitch) < 0){
            midi.beginNote(playingPitch.toMidiFrequency(),instru);
            soundingPitch.add(playingPitch);
        }
        if(recording){
            NoteEvent anotherNote = new NoteEvent(playingPitch, currentTimeMillis(), instru, NoteEvent.Kind.start);
            noteEvents.add(anotherNote);
        }
    }

    void endGoodPitch(Pitch playingPitch, Instrument instru){
        if (soundingPitch.indexOf(playingPitch)>=0){
            midi.endNote(playingPitch.toMidiFrequency(),instru);
            soundingPitch.remove(playingPitch);
        }
        if(recording){
            NoteEvent endingNote = new NoteEvent(playingPitch, currentTimeMillis(), instru, NoteEvent.Kind.stop);
            noteEvents.add(endingNote);
        }
    }
}
class Sound{
    Pitch pitch;
    Instrument instru;
    Sound(Pitch pitch, Instrument instru){
        this.pitch = pitch;
        this.instru = instru;
    }

}