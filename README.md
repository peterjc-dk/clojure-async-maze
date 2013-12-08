# The Clojure Async Maze

![Screen Shot](resources/ScreenShot-2.png)

## Installation

Install Clojure and lein, clone this repo and then do:

```clj
>lein run
```

or

```clj
>lein uberjar

>java -jar target/maze-standalone.jar
```

## Usage

The gameplay is simple, just get the, red dot in the top left corner to the Clojure logo.
 * Use the arrow keys to navigate.
 * Or click with the mouse.
 * press "q" to quit....

The other red dot is just there as a distraction or a opponent. It is possible to both run in day and night mode.


## Implementation details
The main purpose of this app, was for the fun of it, and to play around with clojure, and the clojre core.async library. In the following I will only focus on the specific async part of the implementation.

### Keyboard events
Getting java Keyboard events is simple using seesaw, this snippet show how it can be done. Where "f" is a java swing frame.

```clj
(listen f :key-pressed
   (fn [e] (as/go
              (as/>! e-chan
                 (keys/key-pressed-event-2-key-pressed-map e)))))

```

For every key pressed the event first get converted to a map then put in a channel. In the other end of this channel is a go routine that pulls event maps out, and filter them to different channels as follows


```clj
(defn split-keyword-2-chans
  "Use filter on in chan to relay to out channels"
  [in-chan keyword-set-list]
  (let [cs (repeatedly (count keyword-set-list) as/chan)
        cs-keyword-map (map (fn [keys ch] {:keys keys :chan ch}) keyword-set-list cs)]
    (as/go (while true
             (let [in-val (as/<! in-chan)]
               (doseq [{keys :keys out-chan :chan} cs-keyword-map]
                 (when (contains? keys in-val)
                  (as/>! out-chan in-val))))))
    cs))

```

So when the key-set-list contains  #{:up :down :left :right}, all events in this set is passed to the same channel.

### From actions to moves.
The actions, now converted to keywords are passed on to a go routine that takes the given maze looks up where that action will get you and passes that on, to the GUI updater. Note that

 * It keeps track of the present state.
 * The get-new-position returns next state equals present state if hitting a wall.
 * The s/validate is some fancy "type" validation.
 * The timeout is strictly not needed it just makes it run more smooth.


```clj
(defn get-next-state [state a maze]
  (try
    (let [action (s/validate (s/enum :left :up :down :right) a)]
      (state/get-new-position state action maze))
  (catch Exception e
    state)))

(defn arrow-to-state
  "given a action chan return next state id"
  [in-chan quit-chan maze start-state agent-name timetick]
  (let [out-chan (as/chan)]
    (as/go (as/>! out-chan [agent-name start-state start-state]))
    (as/go (loop [state start-state]
             (let [timeout (as/timeout timetick)
                   [v ch] (as/alts! [quit-chan in-chan timeout])]
               (condp = ch

                 in-chan
                 (let [next-state (get-next-state state v maze)]
                   (as/>! out-chan [agent-name state next-state])
                   (recur next-state))

                 timeout
                 (do
                   (as/>! out-chan [agent-name state state])
                   (recur state))

                 quit-chan
                 (println (str "Stop agent " agent-name))))))
    out-chan))
```

The opponent have a similar looking "agent" that also passes state moves to the GUI updater routine. The maze is just a array that for each position gives legal moves like [:up :left].


As can easily bee seen the logic of the game is clearly isolated from any GUI or Keyboard stuff. Where it is easy to see how a new arrow-key press passes through the code.

## How to test async code?
Since this is a relative new concept in the Clojure world, a best practice for how to write test is a open issue. But most of the heavy lifting can easily be pushed to regular clojure functions that can be tested as usual.

For the above chunk I came up with the following hack(s).


```clj
(defn sq-2-chan [sq]
  (let [c (as/chan)]
    (as/go (loop [s sq]
          (if (and s (first s))
            (do (as/>! c (first s))
                (recur (rest s))) (as/close! c))))
    c))


(defn ch-2-lazy-timeout
  "chan to lazy seq"
  [in-chan timeout]
  (cons (first (as/alts!! [in-chan (as/timeout timeout)]))
        (lazy-seq (ch-2-lazy-timeout in-chan timeout))))

```

With these to nifty helpers I could just
* Generate a sequence of random input data.
* Convert the sequence to channel input
* Put it through the go routine
* Convert it back to a sequence and validate the content

It seems like a dirty hack, but it works.


## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
