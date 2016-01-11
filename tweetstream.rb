require 'tweetstream'
require 'redis'
redis = Redis.new
starting_time = Time.now
TweetStream.configure do |config|
end
STOP_WORDS = ["https"]
TweetStream::Client.new.sample do |status|
  break if Time.now - starting_time > 60 
  words = status.text.scan(/\w+/)
  words.each do |word|
    unless STOP_WORDS.include? word.downcase or word.length < 5
      redis.setnx(word, 0)
      redis.incr(word)
    end
  end
end
top_10 = redis.keys.map{|word| [word, redis.get(word)]}.sort{|w1,w2| w1[1].to_i <=> w2[1].to_i}.reverse[0..10]
# don't include time elapsed as one of the words
puts Hash[top_10]
# program has finished, so redis can be reset now
redis.flushDB
