require 'tweetstream'
require 'redis'

redis = Redis.new # first time using redis, thought would be useful because it's a k-v store
TweetStream.configure do |config|
  # don't include keys and stuff in here cause putting on github
end
STOP_WORDS = ["https"]
# only a few "stop words" were given in the instructions, so I kept running this
# to find more stop words to include in here. Eventually I realized that pretty much
# all the words that were <= 4 characters were stuff like 'with', 'z', 'to', etc.
# which I interpreted as stop words. So I just filtered by word length and didn't include "https" either.
starting_time = Time.now
TweetStream::Client.new.sample do |status|
  break if Time.now - starting_time > 300
  # every iteration, check the time to make sure it has been streaming for < 5 minutes
  words = status.text.scan(/\w+/) # get every word from the tweet
  words.each do |word|
    unless STOP_WORDS.include? word.downcase or word.length < 5
      redis.setnx(word, 0) # if this word does not have a frequency in redis, create it = 0
      redis.incr(word) # add one to the frequency count in redis
    end
  end
end
top_10 = redis.keys # nothing stored in this instance besides words, so just get all keys
              .map{|word| [word, redis.get(word)]} # grab the frequency of each word as well
              .sort{|w1,w2| w1[1].to_i <=> w2[1].to_i} # sort by frequency (2nd item in subarray)
              .reverse # order descending
              .first(10) # take first 10 from sorted data

puts Hash[top_10]
# program has finished, so redis can be reset now
redis.flushDB
