require 'test/unit'
require 'release/poi4r'

class TC_gc < Test::Unit::TestCase
	def test_premature_collection
		h=Poi4r::HSSFWorkbook.new
		h.createSheet("Sheet1");
		5000.times do
			hh=Poi4r::HSSFWorkbook.new
			GC.start()
		end
		assert_equal(1,h.getNumberOfSheets(),"Number of sheets")
	end
end

